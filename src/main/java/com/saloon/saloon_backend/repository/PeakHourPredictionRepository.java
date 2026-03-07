// repository/PeakHourPredictionRepository.java
package com.saloon.saloon_backend.repository;

import com.saloon.saloon_backend.entity.PeakHourPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeakHourPredictionRepository extends JpaRepository<PeakHourPrediction, Long> {

    List<PeakHourPrediction> findByDayOfWeek(Integer dayOfWeek);

    Optional<PeakHourPrediction> findByDayOfWeekAndHourOfDay(Integer dayOfWeek, Integer hourOfDay);

    List<PeakHourPrediction> findByDayOfWeekAndHourOfDayBetween(
            Integer dayOfWeek,
            Integer startHour,
            Integer endHour
    );

    List<PeakHourPrediction> findTop10ByOrderByPredictedBookingsDesc();
}