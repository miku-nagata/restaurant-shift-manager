package com.example.restaurantshiftmanager;

import java.time.LocalDate;

public class ShortageCalendarDay {

    private LocalDate date;
    private boolean hasRequiredStaff;
    private boolean regularHoliday;
    private boolean temporaryClosure;
    private String temporaryClosureReason;
    private int shortageSlotCount;
    private int maxShortageCount;

    public ShortageCalendarDay(LocalDate date, boolean hasRequiredStaff,
                               boolean regularHoliday, boolean temporaryClosure,
                               String temporaryClosureReason,
                               int shortageSlotCount, int maxShortageCount) {
        this.date = date;
        this.hasRequiredStaff = hasRequiredStaff;
        this.regularHoliday = regularHoliday;
        this.temporaryClosure = temporaryClosure;
        this.temporaryClosureReason = temporaryClosureReason;
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

    public boolean isRegularHoliday() {
        return regularHoliday;
    }

    public boolean isTemporaryClosure() {
        return temporaryClosure;
    }

    public String getTemporaryClosureReason() {
        return temporaryClosureReason;
    }

    public int getShortageSlotCount() {
        return shortageSlotCount;
    }

    public int getMaxShortageCount() {
        return maxShortageCount;
    }

    public boolean isShortage() {
        return !regularHoliday && !temporaryClosure && shortageSlotCount > 0;
    }

    public String getStatusText() {
        if (temporaryClosure) {
            return "臨時休業";
        }

        if (regularHoliday) {
            return "定休日";
        }

        if (!hasRequiredStaff) {
            return "未設定";
        }

        if (isShortage()) {
            return "不足 " + shortageSlotCount + "枠";
        }

        return "不足なし";
    }

    public String getSubText() {
        if (temporaryClosure) {
            if (temporaryClosureReason == null || temporaryClosureReason.isBlank()) {
                return "";
            }

            return temporaryClosureReason;
        }

        if (regularHoliday) {
            return "";
        }

        if (!hasRequiredStaff) {
            return "";
        }

        if (isShortage()) {
            return "最大 " + maxShortageCount + "人";
        }

        return "OK";
    }

    public String getStatusClass() {
        if (temporaryClosure) {
            return "temporary-closure-day";
        }

        if (regularHoliday) {
            return "regular-holiday-day";
        }

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