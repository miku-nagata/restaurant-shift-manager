// システム起動時のテスト用データ

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
        // もし従業員の登録が0ならば
        if (employeeRepository.count() == 0) {
            // 名前, 雇用形態, 熟練度, 時給, 月間上限時間, 扶養内限度額
            employeeRepository.save(new Employee("山田 太郎", "アルバイト", "通常", 1000, 80, 88000));
            employeeRepository.save(new Employee("佐藤 花子", "パート", "ベテラン", 1100, 120, 130000));
            employeeRepository.save(new Employee("鈴木 一郎", "正社員", "通常", 0, 176, 0));
        }
    }
}