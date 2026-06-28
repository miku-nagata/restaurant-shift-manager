package com.example.restaurantshiftmanager;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalTime;
import java.util.List;

@Controller
public class RequiredStaffPatternController {

    private final RequiredStaffPatternRepository requiredStaffPatternRepository;

    public RequiredStaffPatternController(RequiredStaffPatternRepository requiredStaffPatternRepository) {
        this.requiredStaffPatternRepository = requiredStaffPatternRepository;
    }

    @GetMapping("/required-staff-patterns")
    public String list(Model model) {
        List<RequiredStaffPattern> patterns = requiredStaffPatternRepository.findAll();
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
            @RequestParam Integer dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam Integer requiredCount,
            Model model
    ) {
        String errorMessage = validatePattern(null, dayOfWeek, startTime, endTime, requiredCount);

        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("timeOptions", createTimeOptions());

            model.addAttribute("dayOfWeek", dayOfWeek);
            model.addAttribute("selectedStartTime", startTime.toString());
            model.addAttribute("selectedEndTime", endTime.toString());
            model.addAttribute("requiredCount", requiredCount);

            return "required-staff-patterns/form";
        }

        RequiredStaffPattern pattern = new RequiredStaffPattern(dayOfWeek, startTime, endTime, requiredCount);
        requiredStaffPatternRepository.save(pattern);

        return "redirect:/required-staff-patterns";
    }

    @PostMapping("/required-staff-patterns/{id}/delete")
    public String delete(@PathVariable Long id) {
        requiredStaffPatternRepository.deleteById(id);

        return "redirect:/required-staff-patterns";
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