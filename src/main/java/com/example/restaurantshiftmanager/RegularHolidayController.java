package com.example.restaurantshiftmanager;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class RegularHolidayController {

    private final RegularHolidayRepository regularHolidayRepository;

    public RegularHolidayController(RegularHolidayRepository regularHolidayRepository) {
        this.regularHolidayRepository = regularHolidayRepository;
    }

    @GetMapping("/regular-holidays")
    public String form(Model model) {
        List<Integer> selectedDayOfWeeks = regularHolidayRepository.findAll()
                .stream()
                .map(RegularHoliday::getDayOfWeek)
                .toList();

        model.addAttribute("selectedDayOfWeeks", selectedDayOfWeeks);

        return "regular-holidays/form";
    }

    @PostMapping("/regular-holidays")
    public String save(
            @RequestParam(required = false) List<Integer> dayOfWeeks,
            Model model
    ) {
        regularHolidayRepository.deleteAll();

        if (dayOfWeeks != null) {
            Set<Integer> uniqueDayOfWeeks = new HashSet<>(dayOfWeeks);

            for (Integer dayOfWeek : uniqueDayOfWeeks) {
                if (dayOfWeek == null || dayOfWeek < 1 || dayOfWeek > 7) {
                    continue;
                }

                regularHolidayRepository.save(new RegularHoliday(dayOfWeek));
            }
        }

        List<Integer> selectedDayOfWeeks = regularHolidayRepository.findAll()
                .stream()
                .map(RegularHoliday::getDayOfWeek)
                .toList();

        model.addAttribute("selectedDayOfWeeks", selectedDayOfWeeks);
        model.addAttribute("successMessage", "定休日設定を保存しました。");

        return "regular-holidays/form";
    }
}