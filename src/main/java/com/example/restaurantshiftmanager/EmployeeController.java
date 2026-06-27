package com.example.restaurantshiftmanager;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

    // 登録フォームを表示する
    @GetMapping("/employees/new")
    public String newForm(Model model) {
        model.addAttribute("employee", new Employee());

        return "employees/form";
    }

    // 登録内容を保存する
    @PostMapping("/employees")
    public String create(@ModelAttribute Employee employee) {
        employeeRepository.save(employee);

        return "redirect:/employees";
    }
}