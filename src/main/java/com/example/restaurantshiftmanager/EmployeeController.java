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
    public String create(@ModelAttribute Employee employee) {
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
    public String update(@PathVariable Long id, @ModelAttribute Employee formEmployee) {
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
}