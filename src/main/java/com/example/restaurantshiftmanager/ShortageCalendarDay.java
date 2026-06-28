package com.example.restaurantshiftmanager;

import java.time.LocalDate;

public class ShortageCalendarDay {

    private LocalDate date;
    private boolean hasRequiredStaff;
    private int shortageSlotCount;
    private int maxShortageCount;

    public ShortageCalendarDay(LocalDate date, boolean hasRequiredStaff,
                               int shortageSlotCount, int maxShortageCount) {
        this.date = date;
        this.hasRequiredStaff = hasRequiredStaff;
        this.shortageSlotCount = shortageSlotCount;
        this.maxShortageCount = maxShortageCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getDayOfMonth() {
        return date.getDayOfMonth();
    }

    public boolean isHasRequiredStaff() {
        return hasRequiredStaff;
    }

    public int getShortageSlotCount() {
        return shortageSlotCount;
    }

    public int getMaxShortageCount() {
        return maxShortageCount;
    }

    public boolean isShortage() {
        return shortageSlotCount > 0;
    }

    public String getStatusText() {
        if (!hasRequiredStaff) {
            return "未設定";
        }

        if (isShortage()) {
            return "不足 " + shortageSlotCount + "枠";
        }

        return "不足なし";
    }

    public String getSubText() {
        if (!hasRequiredStaff) {
            return "";
        }

        if (isShortage()) {
            return "最大 " + maxShortageCount + "人";
        }

        return "OK";
    }

    public String getStatusClass() {
        if (!hasRequiredStaff) {
            return "no-setting-day";
        }

        if (!isShortage()) {
            return "enough-day";
        }

        if (maxShortageCount >= 3 || shortageSlotCount >= 6) {
            return "serious-shortage-day";
        }

        return "shortage-day";
    }
}