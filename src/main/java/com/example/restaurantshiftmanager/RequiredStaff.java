package com.example.restaurantshiftmanager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class RequiredStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate workDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer requiredCount;

    public RequiredStaff() {
    }

    public RequiredStaff(LocalDate workDate, LocalTime startTime, LocalTime endTime, Integer requiredCount) {
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requiredCount = requiredCount;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public Integer getRequiredCount() {
        return requiredCount;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setRequiredCount(Integer requiredCount) {
        this.requiredCount = requiredCount;
    }
}