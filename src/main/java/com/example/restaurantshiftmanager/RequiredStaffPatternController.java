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
import java.util.HashSet;
import java.util.Set;

@Controller
public class RequiredStaffPatternController {

    private final RequiredStaffPatternRepository requiredStaffPatternRepository;
    private final RequiredStaffRepository requiredStaffRepository;
    private final RegularHolidayRepository regularHolidayRepository;
    private final TemporaryClosureRepository temporaryClosureRepository;

    public RequiredStaffPatternController(RequiredStaffPatternRepository requiredStaffPatternRepository,
                                          RequiredStaffRepository requiredStaffRepository,
                                          RegularHolidayRepository regularHolidayRepository,
                                          TemporaryClosureRepository temporaryClosureRepository) {
        this.requiredStaffPatternRepository = requiredStaffPatternRepository;
        this.requiredStaffRepository = requiredStaffRepository;
        this.regularHolidayRepository = regularHolidayRepository;
        this.temporaryClosureRepository = temporaryClosureRepository;
    }

    @GetMapping("/required-staff-patterns")
    public String listRequiredStaffPatterns(Model model) {
        List<RequiredStaffPattern> requiredStaffPatterns = requiredStaffPatternRepository.findAll();

        List<RequiredStaffPatternMatrixRow> matrixRows = createMatrixRows(requiredStaffPatterns);

        model.addAttribute("requiredStaffPatterns", requiredStaffPatterns);
        model.addAttribute("matrixRows", matrixRows);

        return "required-staff-patterns/list";
    }

    @GetMapping("/required-staff-patterns/new")
    public String newForm(Model model) {
        model.addAttribute("timeOptions", createTimeOptions());

        return "required-staff-patterns/form";
    }

    @GetMapping("/required-staff-patterns/bulk/new")
    public String newBulkRequiredStaffPatternForm(Model model) {
        model.addAttribute("timeOptions", createTimeOptions());
        return "required-staff-patterns/bulk-form";
    }

    @PostMapping("/required-staff-patterns")
    public String createRequiredStaffPattern(
            @RequestParam(value = "dayOfWeeks", required = false) List<Integer> dayOfWeeks,

            @RequestParam(value = "startTime", required = false) LocalTime startTime,
            @RequestParam(value = "endTime", required = false) LocalTime endTime,
            @RequestParam(value = "requiredCount", required = false) Integer requiredCount,

            @RequestParam(value = "startTime2", required = false) LocalTime startTime2,
            @RequestParam(value = "endTime2", required = false) LocalTime endTime2,
            @RequestParam(value = "requiredCount2", required = false) Integer requiredCount2,

            @RequestParam(value = "startTime3", required = false) LocalTime startTime3,
            @RequestParam(value = "endTime3", required = false) LocalTime endTime3,
            @RequestParam(value = "requiredCount3", required = false) Integer requiredCount3,

            Model model) {

        List<String> errorMessages = new ArrayList<>();

        if (dayOfWeeks == null || dayOfWeeks.isEmpty()) {
            errorMessages.add("曜日を1つ以上選択してください。");
        }

        int createdCount = 0;

        createdCount += createPatternsForOneTimeSlot(
                "時間帯1",
                dayOfWeeks,
                startTime,
                endTime,
                requiredCount,
                errorMessages
        );

        createdCount += createPatternsForOneTimeSlot(
                "時間帯2",
                dayOfWeeks,
                startTime2,
                endTime2,
                requiredCount2,
                errorMessages
        );

        createdCount += createPatternsForOneTimeSlot(
                "時間帯3",
                dayOfWeeks,
                startTime3,
                endTime3,
                requiredCount3,
                errorMessages
        );

        if (createdCount == 0 && errorMessages.isEmpty()) {
            errorMessages.add("登録する時間帯を1つ以上入力してください。");
        }

        if (!errorMessages.isEmpty()) {
            model.addAttribute("errorMessages", errorMessages);
            model.addAttribute("timeOptions", createTimeOptions());
            model.addAttribute("selectedDayOfWeeks", dayOfWeeks);

            return "required-staff-patterns/form";
        }

        return "redirect:/required-staff-patterns";
    }

    @GetMapping("/required-staff-patterns/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        RequiredStaffPattern pattern = requiredStaffPatternRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("必要人数パターンが見つかりません: " + id));

        model.addAttribute("pattern", pattern);
        model.addAttribute("timeOptions", createTimeOptions());

        return "required-staff-patterns/edit";
    }

    @PostMapping("/required-staff-patterns/{id}/edit")
    public String update(
            @PathVariable Long id,
            @RequestParam Integer dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam Integer requiredCount,
            Model model
    ) {
        RequiredStaffPattern pattern = requiredStaffPatternRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("必要人数パターンが見つかりません: " + id));

        pattern.setDayOfWeek(dayOfWeek);
        pattern.setStartTime(startTime);
        pattern.setEndTime(endTime);
        pattern.setRequiredCount(requiredCount);

        String errorMessage = validatePattern(id, dayOfWeek, startTime, endTime, requiredCount);

        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("pattern", pattern);
            model.addAttribute("timeOptions", createTimeOptions());

            return "required-staff-patterns/edit";
        }

        requiredStaffPatternRepository.save(pattern);

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

        List<RequiredStaff> existingRequiredStaffList =
                new ArrayList<>(requiredStaffRepository.findAll());

        Set<Integer> regularHolidayDayOfWeeks = new HashSet<>();

        for (RegularHoliday regularHoliday : regularHolidayRepository.findAll()) {
            regularHolidayDayOfWeeks.add(regularHoliday.getDayOfWeek());
        }

        Set<LocalDate> temporaryClosureDates = new HashSet<>();

        for (TemporaryClosure temporaryClosure : temporaryClosureRepository.findAll()) {
            LocalDate closureDate = temporaryClosure.getClosureDate();

            if (!closureDate.isBefore(targetMonth.atDay(1))
                    && !closureDate.isAfter(targetMonth.atEndOfMonth())) {
                temporaryClosureDates.add(closureDate);
            }
        }

        int createdCount = 0;
        int skippedCount = 0;
        int holidaySkippedCount = 0;
        int temporaryClosureSkippedCount = 0;

        for (int day = 1; day <= targetMonth.lengthOfMonth(); day++) {
            LocalDate date = targetMonth.atDay(day);
            int dayOfWeek = date.getDayOfWeek().getValue();

            for (RequiredStaffPattern pattern : patterns) {
                if (!pattern.getDayOfWeek().equals(dayOfWeek)) {
                    continue;
                }

                if (regularHolidayDayOfWeeks.contains(dayOfWeek)) {
                    holidaySkippedCount++;
                    continue;
                }

                if (temporaryClosureDates.contains(date)) {
                    temporaryClosureSkippedCount++;
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
                        + " 作成：" + createdCount + "件"
                        + "、重複スキップ：" + skippedCount + "件"
                        + "、定休日スキップ：" + holidaySkippedCount + "件"
                        + "、臨時休業スキップ：" + temporaryClosureSkippedCount + "件");

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

        if (!startTime.isBefore(endTime)) {
            return "開始時刻は終了時刻より前にしてください。";
        }

        if (requiredCount == null || requiredCount < 1) {
            return "必要人数は1人以上で入力してください。";
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

    private List<RequiredStaffPatternMatrixRow> createMatrixRows(List<RequiredStaffPattern> requiredStaffPatterns) {
        List<RequiredStaffPatternMatrixRow> matrixRows = new ArrayList<>();

        for (RequiredStaffPattern pattern : requiredStaffPatterns) {
            RequiredStaffPatternMatrixRow matrixRow = findMatrixRow(
                    matrixRows,
                    pattern.getStartTime(),
                    pattern.getEndTime()
            );

            if (matrixRow == null) {
                matrixRow = new RequiredStaffPatternMatrixRow(
                        pattern.getStartTime(),
                        pattern.getEndTime()
                );

                matrixRows.add(matrixRow);
            }

            matrixRow.setPatternByDay(pattern.getDayOfWeek(), pattern);
        }

        return matrixRows;
    }

    private RequiredStaffPatternMatrixRow findMatrixRow(
            List<RequiredStaffPatternMatrixRow> matrixRows,
            LocalTime startTime,
            LocalTime endTime) {

        for (RequiredStaffPatternMatrixRow matrixRow : matrixRows) {
            boolean sameStartTime = matrixRow.getStartTime().equals(startTime);
            boolean sameEndTime = matrixRow.getEndTime().equals(endTime);

            if (sameStartTime && sameEndTime) {
                return matrixRow;
            }
        }

        return null;
    }

    private int createPatternsForOneTimeSlot(
            String timeSlotName,
            List<Integer> dayOfWeeks,
            LocalTime startTime,
            LocalTime endTime,
            Integer requiredCount,
            List<String> errorMessages) {

        boolean allEmpty = startTime == null && endTime == null && requiredCount == null;

        if (allEmpty) {
            return 0;
        }

        if (dayOfWeeks == null || dayOfWeeks.isEmpty()) {
            return 0;
        }

        if (startTime == null || endTime == null || requiredCount == null) {
            errorMessages.add(timeSlotName + "は、開始時刻・終了時刻・必要人数をすべて入力してください。");
            return 0;
        }

        if (!startTime.isBefore(endTime)) {
            errorMessages.add(timeSlotName + "は、開始時刻を終了時刻より前にしてください。");
            return 0;
        }

        if (requiredCount < 1) {
            errorMessages.add(timeSlotName + "の必要人数は1人以上にしてください。");
            return 0;
        }

        int createdCount = 0;

        for (Integer dayOfWeek : dayOfWeeks) {
            boolean hasOverlap = false;

            List<RequiredStaffPattern> patterns = requiredStaffPatternRepository.findAll();

            for (RequiredStaffPattern pattern : patterns) {
                boolean sameDayOfWeek = pattern.getDayOfWeek().equals(dayOfWeek);

                boolean overlap = startTime.isBefore(pattern.getEndTime())
                        && endTime.isAfter(pattern.getStartTime());

                if (sameDayOfWeek && overlap) {
                    hasOverlap = true;
                    break;
                }
            }

            if (hasOverlap) {
                errorMessages.add(timeSlotName + "は、" + getDayOfWeekName(dayOfWeek)
                        + "に重なる時間帯のパターンがすでに登録されています。");
            } else {
                RequiredStaffPattern requiredStaffPattern = new RequiredStaffPattern();
                requiredStaffPattern.setDayOfWeek(dayOfWeek);
                requiredStaffPattern.setStartTime(startTime);
                requiredStaffPattern.setEndTime(endTime);
                requiredStaffPattern.setRequiredCount(requiredCount);

                requiredStaffPatternRepository.save(requiredStaffPattern);
                createdCount++;
            }
        }

        return createdCount;
    }
}