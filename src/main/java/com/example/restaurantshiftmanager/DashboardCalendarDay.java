package com.example.restaurantshiftmanager;

import java.time.LocalDate;

public class DashboardCalendarDay {

    private LocalDate date;
    private String statusLabel;
    private String statusClass;
    private boolean blank;

    public DashboardCalendarDay() {
    }

    public DashboardCalendarDay(LocalDate date, String statusLabel, String statusClass, boolean blank) {
        this.date = date;
        this.statusLabel = statusLabel;
        this.statusClass = statusClass;
        this.blank = blank;
    }

    public static DashboardCalendarDay blank() {
        return new DashboardCalendarDay(null, "", "", true);
    }

    public static DashboardCalendarDay of(LocalDate date, String statusLabel, String statusClass) {
        return new DashboardCalendarDay(date, statusLabel, statusClass, false);
    }

    public LocalDate getDate() {
        return date;
    }

    public Integer getDayOfMonth() {
        if (date == null) {
            return null;
        }
        return date.getDayOfMonth();
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public String getStatusClass() {
        return statusClass;
    }

    public boolean isBlank() {
        return blank;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public void setStatusClass(String statusClass) {
        this.statusClass = statusClass;
    }

    public void setBlank(boolean blank) {
        this.blank = blank;
    }
}