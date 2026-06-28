package com.example.restaurantshiftmanager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class RegularHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1: 月曜, 2: 火曜, ... 7: 日曜
    private Integer dayOfWeek;

    public RegularHoliday() {
    }

    public RegularHoliday(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Long getId() {
        return id;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
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
}