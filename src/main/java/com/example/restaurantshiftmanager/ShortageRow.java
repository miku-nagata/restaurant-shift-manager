package com.example.restaurantshiftmanager;

import java.time.LocalDate;
import java.time.LocalTime;

public class ShortageRow {

    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer requiredCount;
    private Integer availableCount;
    private Integer shortageCount;

    public ShortageRow(LocalDate workDate, LocalTime startTime, LocalTime endTime,
                       Integer requiredCount, Integer availableCount) {
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requiredCount = requiredCount;
        this.availableCount = availableCount;

        int shortage = requiredCount - availableCount;
        this.shortageCount = Math.max(shortage, 0);
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

    public Integer getAvailableCount() {
        return availableCount;
    }

    public Integer getShortageCount() {
        return shortageCount;
    }

    public boolean isShortage() {
        return shortageCount > 0;
    }

    public String getStatusText() {
        if (isShortage()) {
            return "不足あり";
        }

        return "不足なし";
    }
}