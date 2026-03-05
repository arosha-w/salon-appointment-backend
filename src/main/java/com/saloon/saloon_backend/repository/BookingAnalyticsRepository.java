// repository/BookingAnalyticsRepository.java
package com.saloon.saloon_backend.repository;

import com.saloon.saloon_backend.entity.BookingAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingAnalyticsRepository extends JpaRepository<BookingAnalytics, Long> {

    List<BookingAnalytics> findByDateAfter(LocalDate date);

    List<BookingAnalytics> findByDayOfWeek(Integer dayOfWeek);

    Optional<BookingAnalytics> findByDateAndHourOfDayAndStylistId(
            LocalDate date,
            Integer hourOfDay,
            Long stylistId
    );

    @Query("SELECT ba FROM BookingAnalytics ba WHERE ba.date BETWEEN :startDate AND :endDate ORDER BY ba.date ASC")
    List<BookingAnalytics> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT ba FROM BookingAnalytics ba WHERE ba.dayOfWeek = :dayOfWeek AND ba.hourOfDay = :hourOfDay")
    List<BookingAnalytics> findByDayOfWeekAndHourOfDay(
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("hourOfDay") Integer hourOfDay
    );
}