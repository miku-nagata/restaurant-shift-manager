package com.example.restaurantshiftmanager;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Controller
public class EmployeeShiftController {

    // 従業員情報をデータベースから取得
    private final EmployeeRepository employeeRepository;
    // 作成済みシフトをデータベースから取得するために使う
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    public EmployeeShiftController(
            EmployeeRepository employeeRepository,
            ShiftAssignmentRepository shiftAssignmentRepository) {
        this.employeeRepository = employeeRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
    }

    @GetMapping("/all-shifts")
    public String allShifts(Model model) {
        List<Employee> employees = employeeRepository.findAll();
        model.addAttribute("employees", employees);
        // 現在の月を取得する
        YearMonth targetMonth = YearMonth.now();
        // その月の日付を入れるリスト
        List<LocalDate> dates = new ArrayList<>();
        for (int day = 1; day <= targetMonth.lengthOfMonth(); day++) {
            dates.add(targetMonth.atDay(day));
        }
        model.addAttribute("dates", dates);
        return "all-shifts";
    }
}
