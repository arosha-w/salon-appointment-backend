package com.saloon.saloon_backend.repository;

import com.saloon.saloon_backend.entity.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyStatsRepository extends JpaRepository<DailyStats, Long> {
    Optional<DailyStats> findByStatDate(LocalDate date);

    @Query("SELECT d FROM DailyStats d WHERE d.statDate >= :startDate ORDER BY d.statDate DESC")
    List<DailyStats> findByStatDateAfterOrderByStatDateDesc(LocalDate startDate);

    @Query("SELECT d FROM DailyStats d ORDER BY d.statDate DESC")
    List<DailyStats> findAllOrderByStatDateDesc();
}