package com.example.restaurantshiftmanager;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRequestRepository extends JpaRepository<ShiftRequest, Long> {
}