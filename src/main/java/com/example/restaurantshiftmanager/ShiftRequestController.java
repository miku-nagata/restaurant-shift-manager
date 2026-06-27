package com.example.restaurantshiftmanager;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;

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
            @RequestParam String requestType
    ) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("従業員が見つかりません: " + employeeId));

        ShiftRequest shiftRequest = new ShiftRequest(employee, workDate, startTime, endTime, requestType);

        shiftRequestRepository.save(shiftRequest);

        return "redirect:/shift-requests";
    }

    @PostMapping("/shift-requests/{id}/delete")
    public String delete(@PathVariable Long id) {
        shiftRequestRepository.deleteById(id);

        return "redirect:/shift-requests";
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