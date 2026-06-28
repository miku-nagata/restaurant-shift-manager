package com.example.restaurantshiftmanager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalTime;

@Entity
public class RequiredStaffPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1: 月曜, 2: 火曜, ... 7: 日曜
    private Integer dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer requiredCount;

    public RequiredStaffPattern() {
    }

    public RequiredStaffPattern(Integer dayOfWeek, LocalTime startTime, LocalTime endTime, Integer requiredCount) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requiredCount = requiredCount;
    }

    public Long getId() {
        return id;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
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

    public String getDayOfWeekName() {
        if (dayOfWeek == null) {
            return "";
        }

        return switch (dayOfWeek) {
            case 1 -> "月曜日";
            case 2 -> "火曜日";
            case 3 -> "水曜日";
            case 4 -> "木曜日";
            case 5 -> "金曜日";
            case 6 -> "土曜日";
            case 7 -> "日曜日";
            default -> "";
        };
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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