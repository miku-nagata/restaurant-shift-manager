package com.example.restaurantshiftmanager;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
public class RequiredStaffController {

    private final RequiredStaffRepository requiredStaffRepository;

    public RequiredStaffController(RequiredStaffRepository requiredStaffRepository) {
        this.requiredStaffRepository = requiredStaffRepository;
    }

    @GetMapping("/required-staff")
    public String list(Model model) {
        List<RequiredStaff> requiredStaffList = requiredStaffRepository.findAll();
        model.addAttribute("requiredStaffList", requiredStaffList);
        return "required-staff/list";
    }

    @GetMapping("/required-staff/new")
    public String newForm(Model model) {
        model.addAttribute("timeOptions", createTimeOptions());
        return "required-staff/form";
    }

    @PostMapping("/required-staff")
    public String create(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam Integer requiredCount,
            Model model
    ) {
        String errorMessage = validateRequiredStaff(null, workDate, startTime, endTime, requiredCount);

        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("timeOptions", createTimeOptions());

            model.addAttribute("workDate", workDate);
            model.addAttribute("selectedStartTime", startTime.toString());
            model.addAttribute("selectedEndTime", endTime.toString());
            model.addAttribute("requiredCount", requiredCount);

            return "required-staff/form";
        }

        RequiredStaff requiredStaff = new RequiredStaff(workDate, startTime, endTime, requiredCount);
        requiredStaffRepository.save(requiredStaff);

        return "redirect:/required-staff";
    }

    @GetMapping("/required-staff/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        RequiredStaff requiredStaff = requiredStaffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("必要人数設定が見つかりません: " + id));

        model.addAttribute("requiredStaff", requiredStaff);
        model.addAttribute("timeOptions", createTimeOptions());

        return "required-staff/edit";
    }

    @PostMapping("/required-staff/{id}/edit")
    public String update(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam Integer requiredCount,
            Model model
    ) {
        RequiredStaff requiredStaff = requiredStaffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("必要人数設定が見つかりません: " + id));

        requiredStaff.setWorkDate(workDate);
        requiredStaff.setStartTime(startTime);
        requiredStaff.setEndTime(endTime);
        requiredStaff.setRequiredCount(requiredCount);

        String errorMessage = validateRequiredStaff(id, workDate, startTime, endTime, requiredCount);

        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("requiredStaff", requiredStaff);
            model.addAttribute("timeOptions", createTimeOptions());

            return "required-staff/edit";
        }

        requiredStaffRepository.save(requiredStaff);

        return "redirect:/required-staff";
    }

    @PostMapping("/required-staff/{id}/delete")
    public String delete(@PathVariable Long id) {
        requiredStaffRepository.deleteById(id);
        return "redirect:/required-staff";
    }

    private String validateRequiredStaff(Long currentRequiredStaffId,
                                         LocalDate workDate,
                                         LocalTime startTime,
                                         LocalTime endTime,
                                         Integer requiredCount) {
        if (!startTime.isBefore(endTime)) {
            return "開始時刻は終了時刻より前にしてください。";
        }

        if (requiredCount == null || requiredCount < 1) {
            return "必要人数は1人以上で入力してください。";
        }

        List<RequiredStaff> requiredStaffList = requiredStaffRepository.findAll();

        for (RequiredStaff existingRequiredStaff : requiredStaffList) {
            if (currentRequiredStaffId != null && existingRequiredStaff.getId().equals(currentRequiredStaffId)) {
                continue;
            }

            boolean sameDate = existingRequiredStaff.getWorkDate().equals(workDate);

            boolean overlaps =
                    startTime.isBefore(existingRequiredStaff.getEndTime()) &&
                            endTime.isAfter(existingRequiredStaff.getStartTime());

            if (sameDate && overlaps) {
                return "同じ日付・時間帯に必要人数設定が登録されています。";
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