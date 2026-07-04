// アノテーション
// @Controller      画面担当
// @GetMapping      ページを開いた時の処理
// @PostMapping     フォームが送信された時の処理
// @PathVariable    URLの一部を受け取る

// 従業員の一覧の表示、登録、編集、削除を担当する

package com.example.restaurantshiftmanager;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // 従業員一覧を表示する
    @GetMapping("/employees")
    // Model model = htmlにデータを渡す箱
    public String list(Model model) {
        // データベースから従業員データを取得する
        List<Employee> employees = employeeRepository.findAll();
        // model.addAttribute = htmlで使える名前をつけてデータを入れる
        // 取得したデータをlist.htmlに渡す
        model.addAttribute("employees", employees);
        return "employees/list"; // 表示
    }

    // 新規従業員フォーム
    @GetMapping("/employees/new")
    public String newForm(Model model) {
        // 空のEmployeeをフォームに渡す
        model.addAttribute("employee", new Employee());
        return "employees/form"; // 表示
    }

    // フォームから送信された従業員情報を登録する
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

    // URLのidに入っている従業員データを受け取り、画面編集を表示する
    @GetMapping("/employees/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        /* ラムダ式
        IDに一致する従業員が見つからない場合、エラーを発生させる
        Employee employee = employeeRepository.findById(id)
                必要になったらIllegalArgumentExceptionを作る
                .orElseThrow(() -> new IllegalArgumentException("従業員が見つかりません: " + id));
        model.addAttribute("employee", employee);
        return "employees/edit";
        */
        // 指定されたIDの従業員を探す
        Optional<Employee> result = employeeRepository.findById(id);
        // 見つからなかったらエラーを発生させる
        if (result.isEmpty()) {
            throw new IllegalArgumentException("従業員が見つかりません: " + id);
        }

        // 見つかった従業員データを取り出す
        Employee employee = result.get();

        // 編集画面で使えるように、従業員データを渡す
        model.addAttribute("employee", employee);

        // 編集画面を表示する
        return "employees/edit";
    }

    @PostMapping("/employees/{id}/edit")
    // フォームから送られてきた内容で従業員データを更新する
    public String update(@PathVariable Long id, @ModelAttribute Employee formEmployee, Model model) {
        String errorMessage = validateEmployee(formEmployee);

        if (errorMessage != null) {
            formEmployee.setId(id);
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("employee", formEmployee);
            return "employees/edit";
        }

        // ラムダ式
        // Employee employee = employeeRepository.findById(id)
        //        .orElseThrow(() -> new IllegalArgumentException("従業員が見つかりません: " + id));

        // 指定されたIDの従業員を探す
        Optional<Employee> result = employeeRepository.findById(id);

        // 見つからなかったらエラーを発生させる
        if (result.isEmpty()) {
            throw new IllegalArgumentException("従業員が見つかりません: " + id);
        }

        // 見つかった従業員データを取り出す
        Employee employee = result.get();

        // フォームで入力された内容をデータベースから取得した従業員データに反映する
        employee.setName(formEmployee.getName());
        employee.setEmploymentType(formEmployee.getEmploymentType());
        employee.setSkillLevel(formEmployee.getSkillLevel());
        employee.setHourlyWage(formEmployee.getHourlyWage());
        employee.setMonthlyHourLimit(formEmployee.getMonthlyHourLimit());
        employee.setMonthlyIncomeLimit(formEmployee.getMonthlyIncomeLimit());

        // 更新した従業員データをデータベースに保存する
        employeeRepository.save(employee);

        // 保存後に従業員一覧へ移動する
        return "redirect:/employees";
    }

    // 削除処理、入力チェック処理
    // 指定されたIDの従業員を削除する
    @PostMapping("/employees/{id}/delete")
    public String delete(@PathVariable Long id) {
        // URLのidで受け取った従業員データをデータベースから削除する
        employeeRepository.deleteById(id);
        // 削除したら従業員一覧に戻る
        return "redirect:/employees";
    }

    // 従業員の入力内容をチェックする
    private String validateEmployee(Employee employee) {
        // 氏名がnull または空文字だったらエラーメッセージを返す
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

        // エラーがない場合はnullを返す
        return null;
    }
}