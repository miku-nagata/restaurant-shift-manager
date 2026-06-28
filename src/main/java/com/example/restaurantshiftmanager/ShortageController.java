package com.example.restaurantshiftmanager;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
    public String list(Model model) {
        List<RequiredStaff> requiredStaffList = requiredStaffRepository.findAll();
        List<ShiftRequest> shiftRequests = shiftRequestRepository.findAll();

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

        model.addAttribute("shortageRows", shortageRows);
        return "shortages/list";
    }
}