package com.example.restaurantshiftmanager;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/employees")
    public String list(Model model) {
        List<Employee> employees = employeeRepository.findAll();
        model.addAttribute("employees", employees);
        return "employees/list";
    }

    @GetMapping("/employees/new")
    public String newForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "employees/form";
    }

    @PostMapping("/employees")
    public String create(@ModelAttribute Employee employee, Model model) {
        String errorMessage = validateEmployee(employee);

        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("employee", employee);
            return "employees/form";
        }

        employeeRepository.save(employee);
        return "redirect:/employees";
    }

    @GetMapping("/employees/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("従業員が見つかりません: " + id));
        model.addAttribute("employee", employee);
        return "employees/edit";
    }

    @PostMapping("/employees/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute Employee formEmployee, Model model) {
        String errorMessage = validateEmployee(formEmployee);

        if (errorMessage != null) {
            formEmployee.setId(id);
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("employee", formEmployee);
            return "employees/edit";
        }

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("従業員が見つかりません: " + id));

        employee.setName(formEmployee.getName());
        employee.setEmploymentType(formEmployee.getEmploymentType());
        employee.setSkillLevel(formEmployee.getSkillLevel());
        employee.setHourlyWage(formEmployee.getHourlyWage());
        employee.setMonthlyHourLimit(formEmployee.getMonthlyHourLimit());
        employee.setMonthlyIncomeLimit(formEmployee.getMonthlyIncomeLimit());

        employeeRepository.save(employee);

        return "redirect:/employees";
    }

    @PostMapping("/employees/{id}/delete")
    public String delete(@PathVariable Long id) {
        employeeRepository.deleteById(id);
        return "redirect:/employees";
    }

    private String validateEmployee(Employee employee) {
        if (employee.getName() == null || employee.getName().trim().isEmpty()) {
            return "氏名を入力してください。";
        }

        if (employee.getEmploymentType() == null || employee.getEmploymentType().trim().isEmpty()) {
            return "雇用形態を選択してください。";
        }

        if (employee.getSkillLevel() == null || employee.getSkillLevel().trim().isEmpty()) {
            return "熟練度を選択してください。";
        }

        if (employee.getHourlyWage() == null || employee.getHourlyWage() < 0) {
            return "時給は0以上で入力してください。";
        }

        if (employee.getMonthlyHourLimit() == null || employee.getMonthlyHourLimit() < 0) {
            return "月上限時間は0以上で入力してください。";
        }

        if (employee.getMonthlyIncomeLimit() == null || employee.getMonthlyIncomeLimit() < 0) {
            return "月上限金額は0以上で入力してください。";
        }

        return null;
    }
}