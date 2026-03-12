package com.saloon.saloon_backend.repository;

import com.saloon.saloon_backend.entity.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyStatsRepository extends JpaRepository<DailyStats, Long> {
    Optional<DailyStats> findByStatDate(LocalDate date);

    @Query("SELECT d FROM DailyStats d WHERE d.statDate >= :startDate ORDER BY d.statDate DESC")
    List<DailyStats> findByStatDateAfterOrderByStatDateDesc(LocalDate startDate);

    @Query("SELECT d FROM DailyStats d ORDER BY d.statDate DESC")
    List<DailyStats> findAllOrderByStatDateDesc();

    List<DailyStats> findByStatDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT ds FROM DailyStats ds WHERE ds.statDate >= :startDate ORDER BY ds.statDate DESC")
    List<DailyStats> findRecentStats(@Param("startDate") LocalDate startDate);

    @Query("SELECT SUM(ds.totalRevenue) FROM DailyStats ds WHERE ds.statDate BETWEEN :startDate AND :endDate")
    Double getTotalRevenueForPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}