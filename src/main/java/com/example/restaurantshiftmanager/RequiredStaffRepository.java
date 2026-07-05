package com.example.restaurantshiftmanager;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * 必要人数データをデータベースから取得・保存するためのRepository
 * Repositoryは、データベース操作の窓口
 * JpaRepositoryを継承しているので、基本的な保存・取得・削除は自分で処理を書かなくても使える
 */
public interface RequiredStaffRepository extends JpaRepository<RequiredStaff, Long> {

    /**
     * 指定した日付範囲の必要人数データを取得
     * @param startDate 取得したい期間の開始日
     * @param endDate 取得したい期間の終了日
     * @return 指定した期間に含まれる必要人数データの一覧
     */
    List<RequiredStaff> findByWorkDateBetween(LocalDate startDate, LocalDate endDate);
}