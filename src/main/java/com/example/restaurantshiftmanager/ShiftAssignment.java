package com.example.restaurantshiftmanager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 作成されたシフト結果を表すクラス
 */
@Entity
public class ShiftAssignment {

     // シフト結果を区別するためのID
     // 自動で番号が付けられる
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 勤務する日付
    private LocalDate workDate;

     // 勤務開始時刻
    private LocalTime startTime;

     // 勤務終了時刻
    private LocalTime endTime;


    // このシフトに割り当てられた従業員です。
    // @ManyToOne は「1人の従業員が、複数のシフト結果を持つことができる」という関係を表す
    @ManyToOne
    private Employee employee;

    // 引数なしのコンストラクタJPAがデータベースからデータを取り出すときに必要,
    // 基本的にEntityクラスには用意する
    public ShiftAssignment() {
    }

    /**
     * シフト結果を新しく作るときに使うコンストラクタ
     *
     * @param workDate 勤務日
     * @param startTime 勤務開始時刻
     * @param endTime 勤務終了時刻
     * @param employee 割り当てる従業員
     */
    public ShiftAssignment(LocalDate workDate, LocalTime startTime, LocalTime endTime, Employee employee) {
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.employee = employee;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}