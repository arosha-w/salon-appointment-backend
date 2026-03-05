// repository/SlotConfigurationRepository.java
package com.saloon.saloon_backend.repository;

import com.saloon.saloon_backend.entity.SlotConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlotConfigurationRepository extends JpaRepository<SlotConfiguration, Long> {

    Optional<SlotConfiguration> findByDayOfWeekAndHourOfDay(Integer dayOfWeek, Integer hourOfDay);

    List<SlotConfiguration> findByDayOfWeek(Integer dayOfWeek);

    List<SlotConfiguration> findByIsPeakHourTrue();
}