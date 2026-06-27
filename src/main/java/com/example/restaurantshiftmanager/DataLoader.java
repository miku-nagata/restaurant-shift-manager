package com.example.restaurantshiftmanager;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;

    public DataLoader(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void run(String... args) {
        if (employeeRepository.count() == 0) {
            employeeRepository.save(new Employee("山田 太郎", "アルバイト", "通常"));
            employeeRepository.save(new Employee("佐藤 花子", "パート", "ベテラン"));
            employeeRepository.save(new Employee("鈴木 一郎", "正社員", "通常"));
        }
    }
}
