package com.saloon.saloon_backend.repository;

import com.saloon.saloon_backend.entity.SlotLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlotLockRepository extends JpaRepository<SlotLock, Long> {

    List<SlotLock> findBySessionIdAndIsActiveTrue(String sessionId);

    @Query("SELECT sl FROM SlotLock sl WHERE sl.stylistId = ?1 AND sl.slotDate = ?2 " +
            "AND sl.slotTime = ?3 AND sl.isActive = true")
    Optional<SlotLock> findActiveLock(Long stylistId, LocalDate date, LocalTime time);

    List<SlotLock> findByIsActiveTrueAndExpiresAtBefore(OffsetDateTime expiresAt);

    @Query("SELECT sl FROM SlotLock sl WHERE sl.stylistId = ?1 AND sl.slotDate = ?2 " +
            "AND sl.isActive = true")
    List<SlotLock> findByStylistAndDate(Long stylistId, LocalDate date);
}
