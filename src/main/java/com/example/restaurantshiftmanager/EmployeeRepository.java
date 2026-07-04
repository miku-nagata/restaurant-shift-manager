// 従業員データとデータベースとをやり取りする

// Employee テーブルを操作するためのRepository
// JpaRepositoryを継承　検索、保存、削除などができる

package com.example.restaurantshiftmanager;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}