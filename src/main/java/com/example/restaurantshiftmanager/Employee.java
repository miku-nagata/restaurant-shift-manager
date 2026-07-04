// 従業員１人分のデータ型を決める

package com.example.restaurantshiftmanager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

//　従業員情報をデータベースに保存するためのクラス
@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自動で被りなしの数字割り振り
    private Long id;

    private String name;

    private String employmentType; // 雇用形態

    private String skillLevel; // スキル

    private Integer hourlyWage; //時給

    private Integer monthlyHourLimit; //月間上限時間

    private Integer monthlyIncomeLimit; // 月間扶養内限度額

    // コンストラクタ = クラスから新しい物を作るときに最初に呼ばれるメソッド
    // 空の従業員データを作るためのコンストラクタ
    public Employee() {
    }

    // 名前や雇用形態などを受け取って、従業員データを作るコンストラクタ
    public Employee(String name, String employmentType, String skillLevel,
                    Integer hourlyWage, Integer monthlyHourLimit, Integer monthlyIncomeLimit) {
        this.name = name;
        this.employmentType = employmentType;
        this.skillLevel = skillLevel;
        this.hourlyWage = hourlyWage;
        this.monthlyHourLimit = monthlyHourLimit;
        this.monthlyIncomeLimit = monthlyIncomeLimit;
    }

    // 各データを取得する
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public String getSkillLevel() {
        return skillLevel;
    }

    public Integer getHourlyWage() {
        return hourlyWage;
    }

    public Integer getMonthlyHourLimit() {
        return monthlyHourLimit;
    }

    public Integer getMonthlyIncomeLimit() {
        return monthlyIncomeLimit;
    }

    // フォームとかで受け取った値をEmployeeにセットする
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }

    public void setHourlyWage(Integer hourlyWage) {
        this.hourlyWage = hourlyWage;
    }

    public void setMonthlyHourLimit(Integer monthlyHourLimit) {
        this.monthlyHourLimit = monthlyHourLimit;
    }

    public void setMonthlyIncomeLimit(Integer monthlyIncomeLimit) {
        this.monthlyIncomeLimit = monthlyIncomeLimit;
    }
}