package com.saloon.saloon_backend.repository;

import com.saloon.saloon_backend.entity.StylistWorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StylistWorkingHoursRepository
        extends JpaRepository<StylistWorkingHours, Long> {

    List<StylistWorkingHours>
    findByStylist_IdAndDayOfWeekAndIsActiveTrue(Long stylistId, Integer dayOfWeek);
}
