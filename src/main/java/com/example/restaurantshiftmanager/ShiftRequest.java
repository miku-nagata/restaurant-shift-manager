package com.example.restaurantshiftmanager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class ShiftRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalDate workDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private String requestType;

    public ShiftRequest() {
    }

    public ShiftRequest(Employee employee, LocalDate workDate, LocalTime startTime, LocalTime endTime, String requestType) {
        this.employee = employee;
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requestType = requestType;
    }

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
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

    public String getRequestType() {
        return requestType;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
}