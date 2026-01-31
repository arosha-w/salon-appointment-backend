package com.saloon.saloon_backend.repository;

import com.saloon.saloon_backend.entity.StylistTimeOff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface StylistTimeOffRepository
        extends JpaRepository<StylistTimeOff, Long> {

    List<StylistTimeOff>
    findByStylist_IdAndEndTsGreaterThanAndStartTsLessThan(
            Long stylistId,
            OffsetDateTime start,
            OffsetDateTime end
    );
}
