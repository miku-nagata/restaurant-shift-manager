package com.example.restaurantshiftmanager;

import java.time.LocalTime;

public class RequiredStaffPatternMatrixRow {

    private LocalTime startTime;
    private LocalTime endTime;

    private RequiredStaffPattern mondayPattern;
    private RequiredStaffPattern tuesdayPattern;
    private RequiredStaffPattern wednesdayPattern;
    private RequiredStaffPattern thursdayPattern;
    private RequiredStaffPattern fridayPattern;
    private RequiredStaffPattern saturdayPattern;
    private RequiredStaffPattern sundayPattern;

    public RequiredStaffPatternMatrixRow(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void setPatternByDay(Integer dayOfWeek, RequiredStaffPattern pattern) {
        if (dayOfWeek == null) {
            return;
        }

        if (dayOfWeek == 1) {
            this.mondayPattern = pattern;
        } else if (dayOfWeek == 2) {
            this.tuesdayPattern = pattern;
        } else if (dayOfWeek == 3) {
            this.wednesdayPattern = pattern;
        } else if (dayOfWeek == 4) {
            this.thursdayPattern = pattern;
        } else if (dayOfWeek == 5) {
            this.fridayPattern = pattern;
        } else if (dayOfWeek == 6) {
            this.saturdayPattern = pattern;
        } else if (dayOfWeek == 7) {
            this.sundayPattern = pattern;
        }
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public RequiredStaffPattern getMondayPattern() {
        return mondayPattern;
    }

    public RequiredStaffPattern getTuesdayPattern() {
        return tuesdayPattern;
    }

    public RequiredStaffPattern getWednesdayPattern() {
        return wednesdayPattern;
    }

    public RequiredStaffPattern getThursdayPattern() {
        return thursdayPattern;
    }

    public RequiredStaffPattern getFridayPattern() {
        return fridayPattern;
    }

    public RequiredStaffPattern getSaturdayPattern() {
        return saturdayPattern;
    }

    public RequiredStaffPattern getSundayPattern() {
        return sundayPattern;
    }
}