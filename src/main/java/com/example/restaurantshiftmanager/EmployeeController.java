package com.example.restaurantshiftmanager;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class EmployeeController {

    @GetMapping("/employees")
    public String list(Model model) {
        List<Employee> employees = List.of(
                new Employee(1L, "山田 太郎", "アルバイト", "通常"),
                new Employee(2L, "佐藤 花子", "パート", "ベテラン"),
                new Employee(3L, "鈴木 一郎", "正社員", "通常")
        );

        // employeesという名前で、従業員一覧をHTMLに渡す
        model.addAttribute("employees", employees);

        return "employees/list";
    }
}