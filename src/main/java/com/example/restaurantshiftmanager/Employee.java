package com.example.restaurantshiftmanager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// このjavaクラスをDBのテーブルとして扱う
@Entity
public class Employee {

    // id を主キーにして、自動採番する
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String employmentType;

    private String skillLevel;

    public Employee() {
    }

    public Employee(String name, String employmentType, String skillLevel) {
        this.name = name;
        this.employmentType = employmentType;
        this.skillLevel = skillLevel;
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
}