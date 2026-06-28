package com.example.restaurantshiftmanager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class TemporaryClosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate closureDate;

    private String reason;

    public TemporaryClosure() {
    }

    public TemporaryClosure(LocalDate closureDate, String reason) {
        this.closureDate = closureDate;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getClosureDate() {
        return closureDate;
    }

    public String getReason() {
        return reason;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setClosureDate(LocalDate closureDate) {
        this.closureDate = closureDate;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}