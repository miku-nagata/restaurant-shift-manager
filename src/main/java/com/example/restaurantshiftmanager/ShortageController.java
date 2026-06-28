package com.example.restaurantshiftmanager;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

@Controller
public class ShortageController {

    private final RequiredStaffRepository requiredStaffRepository;
    private final ShiftRequestRepository shiftRequestRepository;

    public ShortageController(RequiredStaffRepository requiredStaffRepository,
                              ShiftRequestRepository shiftRequestRepository) {
        this.requiredStaffRepository = requiredStaffRepository;
        this.shiftRequestRepository = shiftRequestRepository;
    }

    @GetMapping("/shortages")
    public String list(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model
    ) {
        List<RequiredStaff> requiredStaffList = requiredStaffRepository.findAll();
        List<ShiftRequest> shiftRequests = shiftRequestRepository.findAll();

        if (date != null) {
            requiredStaffList = requiredStaffList.stream()
                    .filter(requiredStaff -> requiredStaff.getWorkDate().equals(date))
                    .toList();
        }

        List<ShortageRow> shortageRows = createShortageRows(requiredStaffList, shiftRequests);

        model.addAttribute("shortageRows", shortageRows);
        model.addAttribute("selectedDate", date);

        return "shortages/list";
    }

    @GetMapping("/shortages/calendar")
    public String calendar(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model
    ) {
        YearMonth targetMonth;

        if (year == null || month == null) {
            targetMonth = YearMonth.now();
        } else {
            targetMonth = YearMonth.of(year, month);
        }

        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd = targetMonth.atEndOfMonth();

        List<RequiredStaff> requiredStaffList = requiredStaffRepository.findAll();
        List<ShiftRequest> shiftRequests = shiftRequestRepository.findAll();

        List<RequiredStaff> monthlyRequiredStaffList = requiredStaffList.stream()
                .filter(requiredStaff ->
                        !requiredStaff.getWorkDate().isBefore(monthStart)
                                && !requiredStaff.getWorkDate().isAfter(monthEnd))
                .toList();

        List<ShortageRow> shortageRows = createShortageRows(monthlyRequiredStaffList, shiftRequests);

        Map<LocalDate, List<ShortageRow>> rowsByDate = new HashMap<>();
        for (ShortageRow row : shortageRows) {
            rowsByDate.computeIfAbsent(row.getWorkDate(), key -> new ArrayList<>()).add(row);
        }

        Set<LocalDate> datesWithRequiredStaff = new HashSet<>();
        for (RequiredStaff requiredStaff : monthlyRequiredStaffList) {
            datesWithRequiredStaff.add(requiredStaff.getWorkDate());
        }

        List<List<ShortageCalendarDay>> calendarWeeks = createCalendarWeeks(
                targetMonth,
                rowsByDate,
                datesWithRequiredStaff
        );

        int shortageDayCount = 0;
        int noSettingDayCount = 0;
        int maxShortageCount = 0;
        LocalDate mostDangerousDate = null;
        int mostDangerousSlotCount = 0;

        for (List<ShortageCalendarDay> week : calendarWeeks) {
            for (ShortageCalendarDay day : week) {
                if (day == null) {
                    continue;
                }

                if (!day.isHasRequiredStaff()) {
                    noSettingDayCount++;
                }

                if (day.isShortage()) {
                    shortageDayCount++;
                }

                if (day.getMaxShortageCount() > maxShortageCount) {
                    maxShortageCount = day.getMaxShortageCount();
                }

                if (day.getShortageSlotCount() > mostDangerousSlotCount) {
                    mostDangerousSlotCount = day.getShortageSlotCount();
                    mostDangerousDate = day.getDate();
                }
            }
        }

        YearMonth previousMonth = targetMonth.minusMonths(1);
        YearMonth nextMonth = targetMonth.plusMonths(1);

        model.addAttribute("calendarWeeks", calendarWeeks);
        model.addAttribute("targetMonth", targetMonth);
        model.addAttribute("previousYear", previousMonth.getYear());
        model.addAttribute("previousMonth", previousMonth.getMonthValue());
        model.addAttribute("nextYear", nextMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());

        model.addAttribute("shortageDayCount", shortageDayCount);
        model.addAttribute("noSettingDayCount", noSettingDayCount);
        model.addAttribute("maxShortageCount", maxShortageCount);
        model.addAttribute("mostDangerousDate", mostDangerousDate);

        return "shortages/calendar";
    }

    private List<ShortageRow> createShortageRows(List<RequiredStaff> requiredStaffList,
                                                 List<ShiftRequest> shiftRequests) {
        List<ShortageRow> shortageRows = new ArrayList<>();

        for (RequiredStaff requiredStaff : requiredStaffList) {
            LocalTime slotStart = requiredStaff.getStartTime();

            while (slotStart.isBefore(requiredStaff.getEndTime())) {
                LocalTime slotEnd = slotStart.plusMinutes(30);

                int availableCount = 0;

                for (ShiftRequest shiftRequest : shiftRequests) {
                    boolean sameDate = shiftRequest.getWorkDate().equals(requiredStaff.getWorkDate());
                    boolean isAvailable = "出勤希望".equals(shiftRequest.getRequestType());

                    boolean coversSlotStart = !shiftRequest.getStartTime().isAfter(slotStart);
                    boolean coversSlotEnd = !shiftRequest.getEndTime().isBefore(slotEnd);

                    if (sameDate && isAvailable && coversSlotStart && coversSlotEnd) {
                        availableCount++;
                    }
                }

                ShortageRow row = new ShortageRow(
                        requiredStaff.getWorkDate(),
                        slotStart,
                        slotEnd,
                        requiredStaff.getRequiredCount(),
                        availableCount
                );

                shortageRows.add(row);

                slotStart = slotEnd;
            }
        }

        return shortageRows;
    }

    private List<List<ShortageCalendarDay>> createCalendarWeeks(
            YearMonth targetMonth,
            Map<LocalDate, List<ShortageRow>> rowsByDate,
            Set<LocalDate> datesWithRequiredStaff
    ) {
        List<List<ShortageCalendarDay>> calendarWeeks = new ArrayList<>();

        LocalDate firstDay = targetMonth.atDay(1);
        int blankCount = firstDay.getDayOfWeek().getValue() % 7;

        List<ShortageCalendarDay> week = new ArrayList<>();

        for (int i = 0; i < blankCount; i++) {
            week.add(null);
        }

        for (int day = 1; day <= targetMonth.lengthOfMonth(); day++) {
            LocalDate date = targetMonth.atDay(day);
            List<ShortageRow> rows = rowsByDate.getOrDefault(date, new ArrayList<>());

            boolean hasRequiredStaff = datesWithRequiredStaff.contains(date);

            int shortageSlotCount = 0;
            int maxShortageCount = 0;

            for (ShortageRow row : rows) {
                if (row.isShortage()) {
                    shortageSlotCount++;
                }

                if (row.getShortageCount() > maxShortageCount) {
                    maxShortageCount = row.getShortageCount();
                }
            }

            week.add(new ShortageCalendarDay(
                    date,
                    hasRequiredStaff,
                    shortageSlotCount,
                    maxShortageCount
            ));

            if (week.size() == 7) {
                calendarWeeks.add(week);
                week = new ArrayList<>();
            }
        }

        if (!week.isEmpty()) {
            while (week.size() < 7) {
                week.add(null);
            }

            calendarWeeks.add(week);
        }

        return calendarWeeks;
    }
}