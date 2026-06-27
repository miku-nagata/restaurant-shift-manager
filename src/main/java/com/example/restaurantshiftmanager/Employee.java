package com.example.restaurantshiftmanager;

public class Employee {

    private Long id;
    private String name;
    private String employmentType;
    private String skillLevel;

    public Employee(Long id, String name, String employmentType, String skillLevel) {
        this.id = id;
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
}
