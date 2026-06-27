package com.example.restaurantshiftmanager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String employmentType;

    private String skillLevel;

    private Integer hourlyWage;

    private Integer monthlyHourLimit;

    private Integer monthlyIncomeLimit;

    public Employee() {
    }

    public Employee(String name, String employmentType, String skillLevel,
                    Integer hourlyWage, Integer monthlyHourLimit, Integer monthlyIncomeLimit) {
        this.name = name;
        this.employmentType = employmentType;
        this.skillLevel = skillLevel;
        this.hourlyWage = hourlyWage;
        this.monthlyHourLimit = monthlyHourLimit;
        this.monthlyIncomeLimit = monthlyIncomeLimit;
    }

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