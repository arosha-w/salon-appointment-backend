package com.saloon.saloon_backend.repository;

import com.saloon.saloon_backend.entity.Appointment;
import com.saloon.saloon_backend.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    List<Appointment>
    findByStylist_IdAndStatusInAndEndTsGreaterThanAndStartTsLessThan(
            Long stylistId,
            List<AppointmentStatus> statuses,
            OffsetDateTime start,
            OffsetDateTime end
    );
    @Query("""
        select count(a) > 0 from Appointment a
        where a.stylist.id = :stylistId
          and a.status <> 'CANCELLED'
          and (a.startTs < :endTs and a.endTs > :startTs)
    """)
    boolean existsOverlap(Long stylistId, OffsetDateTime startTs, OffsetDateTime endTs);
}
