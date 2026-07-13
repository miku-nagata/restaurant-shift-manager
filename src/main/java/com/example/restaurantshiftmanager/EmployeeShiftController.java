package com.example.restaurantshiftmanager;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


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
        // 今月の初日と最終日
        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();
        // 今月のシフトを取得
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByWorkDateBetween(startDate, endDate);
        // 日付と従業員IDから対応するシフトを探す
        Map<LocalDate, Map<Long, ShiftAssignment>> assignmentMap = new HashMap<>();

        for (ShiftAssignment assignment : assignments) {

            // 勤務日を取得
            LocalDate workDate = assignment.getWorkDate();

            // 割り当てられている従業員のIDを取得
            Long employeeId = assignment.getEmployee().getId();

            // その日付のMapがなければ新しく作る
            if (!assignmentMap.containsKey(workDate)) {
                assignmentMap.put(workDate, new HashMap<>());
            }

            // 日付のMapに従業員IDとシフトを登録する
            assignmentMap.get(workDate).put(employeeId, assignment);
        }

        model.addAttribute("assignments", assignments);
        model.addAttribute("assignmentMap", assignmentMap);
        // その月の日付を入れるリスト
        List<LocalDate> dates = new ArrayList<>();
        for (int day = 1; day <= targetMonth.lengthOfMonth(); day++) {
            dates.add(targetMonth.atDay(day));
        }
        model.addAttribute("dates", dates);
        return "all-shifts";
    }
}
