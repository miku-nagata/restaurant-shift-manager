package com.example.restaurantshiftmanager;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * ShiftAssignment用のRepository
 * Repositoryは、データベースとやり取りするためのクラス
 * このRepositoryを使うことで、シフト結果を保存したり、一覧取得したり、削除したりできる。
 */
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {

    /**
     * 指定した日付範囲のシフト結果を取得
     * @param startDate 取得したい期間の開始日
     * @param endDate 取得したい期間の終了日
     * @return 指定した期間に含まれるシフト結果の一覧
     */
    List<ShiftAssignment> findByWorkDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 指定した日付範囲のシフト結果を削除
     * 自動作成をやり直すときに、
     * すでに作成済みのシフトを一度消してから再作成するために使用
     * @param startDate 削除したい期間の開始日
     * @param endDate 削除したい期間の終了日
     */
    void deleteByWorkDateBetween(LocalDate startDate, LocalDate endDate);
}