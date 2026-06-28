package com.example.restaurantshiftmanager;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
public class RequiredStaffPatternController {

    private final RequiredStaffPatternRepository requiredStaffPatternRepository;
    private final RequiredStaffRepository requiredStaffRepository;

    public RequiredStaffPatternController(RequiredStaffPatternRepository requiredStaffPatternRepository,
                                          RequiredStaffRepository requiredStaffRepository) {
        this.requiredStaffPatternRepository = requiredStaffPatternRepository;
        this.requiredStaffRepository = requiredStaffRepository;
    }

    @GetMapping("/required-staff-patterns")
    public String list(Model model) {
        List<RequiredStaffPattern> patterns = requiredStaffPatternRepository.findAll()
                .stream()
                .sorted(Comparator
                        .comparing(RequiredStaffPattern::getDayOfWeek)
                        .thenComparing(RequiredStaffPattern::getStartTime))
                .toList();

        model.addAttribute("patterns", patterns);

        return "required-staff-patterns/list";
    }

    @GetMapping("/required-staff-patterns/new")
    public String newForm(Model model) {
        model.addAttribute("timeOptions", createTimeOptions());

        return "required-staff-patterns/form";
    }

    @PostMapping("/required-staff-patterns")
    public String create(
            @RequestParam(required = false) List<Integer> dayOfWeeks,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam Integer requiredCount,
            Model model
    ) {
        String errorMessage = validateSelectedDays(dayOfWeeks, startTime, endTime, requiredCount);

        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("timeOptions", createTimeOptions());

            model.addAttribute("dayOfWeeks", dayOfWeeks);
            model.addAttribute("selectedStartTime", startTime.toString());
            model.addAttribute("selectedEndTime", endTime.toString());
            model.addAttribute("requiredCount", requiredCount);

            return "required-staff-patterns/form";
        }

        for (Integer dayOfWeek : dayOfWeeks) {
            RequiredStaffPattern pattern = new RequiredStaffPattern(
                    dayOfWeek,
                    startTime,
                    endTime,
                    requiredCount
            );

            requiredStaffPatternRepository.save(pattern);
        }

        return "redirect:/required-staff-patterns";
    }

    @GetMapping("/required-staff-patterns/apply")
    public String applyForm(Model model) {
        YearMonth now = YearMonth.now();

        model.addAttribute("year", now.getYear());
        model.addAttribute("month", now.getMonthValue());

        return "required-staff-patterns/apply";
    }

    @PostMapping("/required-staff-patterns/apply")
    public String apply(
            @RequestParam Integer year,
            @RequestParam Integer month,
            Model model
    ) {
        if (year == null || month == null || month < 1 || month > 12) {
            model.addAttribute("errorMessage", "反映する年月を正しく入力してください。");
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            return "required-staff-patterns/apply";
        }

        List<RequiredStaffPattern> patterns = requiredStaffPatternRepository.findAll();

        if (patterns.isEmpty()) {
            model.addAttribute("errorMessage", "必要人数パターンが登録されていません。");
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            return "required-staff-patterns/apply";
        }

        YearMonth targetMonth = YearMonth.of(year, month);

        List<RequiredStaff> existingRequiredStaffList = new ArrayList<>(requiredStaffRepository.findAll());

        int createdCount = 0;
        int skippedCount = 0;

        for (int day = 1; day <= targetMonth.lengthOfMonth(); day++) {
            LocalDate date = targetMonth.atDay(day);
            int dayOfWeek = date.getDayOfWeek().getValue();

            for (RequiredStaffPattern pattern : patterns) {
                if (!pattern.getDayOfWeek().equals(dayOfWeek)) {
                    continue;
                }

                boolean overlaps = existsOverlappingRequiredStaff(
                        existingRequiredStaffList,
                        date,
                        pattern.getStartTime(),
                        pattern.getEndTime()
                );

                if (overlaps) {
                    skippedCount++;
                    continue;
                }

                RequiredStaff requiredStaff = new RequiredStaff(
                        date,
                        pattern.getStartTime(),
                        pattern.getEndTime(),
                        pattern.getRequiredCount()
                );

                requiredStaffRepository.save(requiredStaff);
                existingRequiredStaffList.add(requiredStaff);

                createdCount++;
            }
        }

        model.addAttribute("successMessage",
                year + "年" + month + "月へ必要人数パターンを反映しました。"
                        + " 作成：" + createdCount + "件、スキップ：" + skippedCount + "件");

        model.addAttribute("year", year);
        model.addAttribute("month", month);

        return "required-staff-patterns/apply";
    }

    @PostMapping("/required-staff-patterns/{id}/delete")
    public String delete(@PathVariable Long id) {
        requiredStaffPatternRepository.deleteById(id);

        return "redirect:/required-staff-patterns";
    }

    private boolean existsOverlappingRequiredStaff(List<RequiredStaff> requiredStaffList,
                                                   LocalDate workDate,
                                                   LocalTime startTime,
                                                   LocalTime endTime) {
        for (RequiredStaff existingRequiredStaff : requiredStaffList) {
            boolean sameDate = existingRequiredStaff.getWorkDate().equals(workDate);

            boolean overlaps =
                    startTime.isBefore(existingRequiredStaff.getEndTime()) &&
                            endTime.isAfter(existingRequiredStaff.getStartTime());

            if (sameDate && overlaps) {
                return true;
            }
        }

        return false;
    }

    private String validateSelectedDays(List<Integer> dayOfWeeks,
                                        LocalTime startTime,
                                        LocalTime endTime,
                                        Integer requiredCount) {
        if (dayOfWeeks == null || dayOfWeeks.isEmpty()) {
            return "曜日を1つ以上選択してください。";
        }

        if (!startTime.isBefore(endTime)) {
            return "開始時刻は終了時刻より前にしてください。";
        }

        if (requiredCount == null || requiredCount < 1) {
            return "必要人数は1人以上で入力してください。";
        }

        for (Integer dayOfWeek : dayOfWeeks) {
            String errorMessage = validatePattern(null, dayOfWeek, startTime, endTime, requiredCount);

            if (errorMessage != null) {
                return getDayOfWeekName(dayOfWeek) + "：" + errorMessage;
            }
        }

        return null;
    }

    private String validatePattern(Long currentPatternId,
                                   Integer dayOfWeek,
                                   LocalTime startTime,
                                   LocalTime endTime,
                                   Integer requiredCount) {
        if (dayOfWeek == null || dayOfWeek < 1 || dayOfWeek > 7) {
            return "曜日を選択してください。";
        }

        List<RequiredStaffPattern> patterns = requiredStaffPatternRepository.findAll();

        for (RequiredStaffPattern existingPattern : patterns) {
            if (currentPatternId != null && existingPattern.getId().equals(currentPatternId)) {
                continue;
            }

            boolean sameDayOfWeek = existingPattern.getDayOfWeek().equals(dayOfWeek);

            boolean overlaps =
                    startTime.isBefore(existingPattern.getEndTime()) &&
                            endTime.isAfter(existingPattern.getStartTime());

            if (sameDayOfWeek && overlaps) {
                return "同じ曜日・時間帯に必要人数パターンが登録されています。";
            }
        }

        return null;
    }

    private String getDayOfWeekName(Integer dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "月曜日";
            case 2 -> "火曜日";
            case 3 -> "水曜日";
            case 4 -> "木曜日";
            case 5 -> "金曜日";
            case 6 -> "土曜日";
            case 7 -> "日曜日";
            default -> "";
        };
    }

    private List<String> createTimeOptions() {
        return List.of(
                "09:00", "09:30",
                "10:00", "10:30",
                "11:00", "11:30",
                "12:00", "12:30",
                "13:00", "13:30",
                "14:00", "14:30",
                "15:00", "15:30",
                "16:00", "16:30",
                "17:00", "17:30",
                "18:00", "18:30",
                "19:00", "19:30",
                "20:00", "20:30",
                "21:00", "21:30",
                "22:00"
        );
    }
}