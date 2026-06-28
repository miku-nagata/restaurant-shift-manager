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
import java.util.List;

@Controller
public class ShiftRequestController {

    private final ShiftRequestRepository shiftRequestRepository;
    private final EmployeeRepository employeeRepository;

    public ShiftRequestController(ShiftRequestRepository shiftRequestRepository,
                                  EmployeeRepository employeeRepository) {
        this.shiftRequestRepository = shiftRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/shift-requests")
    public String list(Model model) {
        List<ShiftRequest> shiftRequests = shiftRequestRepository.findAll();
        model.addAttribute("shiftRequests", shiftRequests);
        return "shift-requests/list";
    }

    @GetMapping("/shift-requests/new")
    public String newForm(Model model) {
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("timeOptions", createTimeOptions());
        return "shift-requests/form";
    }

    @PostMapping("/shift-requests")
    public String create(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam String requestType,
            Model model
    ) {
        String errorMessage = validateShiftRequest(null, employeeId, workDate, startTime, endTime);

        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);

            model.addAttribute("employees", employeeRepository.findAll());
            model.addAttribute("timeOptions", createTimeOptions());

            model.addAttribute("selectedEmployeeId", employeeId);
            model.addAttribute("workDate", workDate);
            model.addAttribute("selectedStartTime", startTime.toString());
            model.addAttribute("selectedEndTime", endTime.toString());
            model.addAttribute("selectedRequestType", requestType);

            return "shift-requests/form";
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("従業員が見つかりません: " + employeeId));

        ShiftRequest shiftRequest = new ShiftRequest(employee, workDate, startTime, endTime, requestType);
        shiftRequestRepository.save(shiftRequest);

        return "redirect:/shift-requests";
    }

    @GetMapping("/shift-requests/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ShiftRequest shiftRequest = shiftRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("勤務希望が見つかりません: " + id));

        model.addAttribute("shiftRequest", shiftRequest);
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("timeOptions", createTimeOptions());

        return "shift-requests/edit";
    }

    @PostMapping("/shift-requests/{id}/edit")
    public String update(
            @PathVariable Long id,
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam String requestType,
            Model model
    ) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("従業員が見つかりません: " + employeeId));

        ShiftRequest shiftRequest = shiftRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("勤務希望が見つかりません: " + id));

        shiftRequest.setEmployee(employee);
        shiftRequest.setWorkDate(workDate);
        shiftRequest.setStartTime(startTime);
        shiftRequest.setEndTime(endTime);
        shiftRequest.setRequestType(requestType);

        String errorMessage = validateShiftRequest(id, employeeId, workDate, startTime, endTime);

        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("shiftRequest", shiftRequest);
            model.addAttribute("employees", employeeRepository.findAll());
            model.addAttribute("timeOptions", createTimeOptions());

            return "shift-requests/edit";
        }

        shiftRequestRepository.save(shiftRequest);

        return "redirect:/shift-requests";
    }

    @PostMapping("/shift-requests/{id}/delete")
    public String delete(@PathVariable Long id) {
        shiftRequestRepository.deleteById(id);
        return "redirect:/shift-requests";
    }

    private String validateShiftRequest(Long currentRequestId,
                                        Long employeeId,
                                        LocalDate workDate,
                                        LocalTime startTime,
                                        LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            return "開始時刻は終了時刻より前にしてください。";
        }

        List<ShiftRequest> shiftRequests = shiftRequestRepository.findAll();

        for (ShiftRequest existingRequest : shiftRequests) {
            if (currentRequestId != null && existingRequest.getId().equals(currentRequestId)) {
                continue;
            }

            boolean sameEmployee = existingRequest.getEmployee().getId().equals(employeeId);
            boolean sameDate = existingRequest.getWorkDate().equals(workDate);

            boolean overlaps =
                    startTime.isBefore(existingRequest.getEndTime()) &&
                            endTime.isAfter(existingRequest.getStartTime());

            if (sameEmployee && sameDate && overlaps) {
                return "同じ従業員の同じ日付・時間帯に勤務希望が登録されています。";
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