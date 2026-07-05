package com.example.restaurantshiftmanager;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * 勤務希望データをデータベースから取得・保存するためのRepository
 * Repositoryは、データベース操作の窓口になるもの
 * JpaRepositoryを継承しているので、save・findAll・findById・deleteなどは自分で書かなくても使える
 */
public interface ShiftRequestRepository extends JpaRepository<ShiftRequest, Long> {

    /**
     * 指定した日付範囲の勤務希望を取得
     * @param startDate 取得したい期間の開始日
     * @param endDate 取得したい期間の終了日
     * @return 指定した期間に含まれる勤務希望の一覧
     */
    List<ShiftRequest> findByWorkDateBetween(LocalDate startDate, LocalDate endDate);
}