// ============================================================================
// FILE: service/SlotLockService.java
// PURPOSE: Manages temporary slot locks to prevent overbooking
// FEATURES:
// - Lock slots for 5 minutes during booking
// - Auto-cleanup of expired locks
// - Extend lock when user is still on page
// - Release locks on booking completion
// ============================================================================
package com.saloon.saloon_backend.service;

import com.saloon.saloon_backend.dto.SlotLockRequestDTO;
import com.saloon.saloon_backend.dto.SlotLockResponseDTO;
import com.saloon.saloon_backend.entity.SlotLock;
import com.saloon.saloon_backend.repository.SlotLockRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class SlotLockService {

    private final SlotLockRepository slotLockRepository;
    private static final int LOCK_DURATION_MINUTES = 3;

    public SlotLockService(SlotLockRepository slotLockRepository) {
        this.slotLockRepository = slotLockRepository;
    }

    /**
     * Lock a time slot temporarily (5 minutes)
     * Prevents concurrent bookings of the same slot
     */
    @Transactional
    public SlotLockResponseDTO lockSlot(
            SlotLockRequestDTO request,
            String userEmail
    ) {
        SlotLockResponseDTO response = new SlotLockResponseDTO();

        try {
            // Parse date and time
            LocalDate date = LocalDate.parse(request.getDate());
            LocalTime time = LocalTime.parse(request.getTime());

            // Check if slot is already locked
            Optional<SlotLock> existingLock = slotLockRepository.findActiveLock(
                    request.getStylistId(),
                    date,
                    time
            );

            if (existingLock.isPresent()) {
                SlotLock lock = existingLock.get();

                // Check if lock is expired
                if (lock.isExpired()) {
                    // Deactivate expired lock
                    lock.setIsActive(false);
                    slotLockRepository.save(lock);
                } else {
                    // Check if it's the same user extending their lock
                    if (lock.getSessionId().equals(request.getSessionId())) {
                        // Extend the existing lock
                        lock.setExpiresAt(OffsetDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                        slotLockRepository.save(lock);

                        response.setLockId(lock.getId());
                        response.setSessionId(lock.getSessionId());
                        response.setExpiresAt(lock.getExpiresAt().toString());
                        response.setSecondsRemaining(
                                (int) Duration.between(OffsetDateTime.now(), lock.getExpiresAt()).toSeconds()
                        );
                        response.setSuccess(true);
                        response.setMessage("Lock extended successfully");
                        return response;
                    }

                    // Slot is locked by another user
                    response.setSuccess(false);
                    response.setMessage("This slot is currently being booked by another user. " +
                            "Please try again in a moment.");
                    return response;
                }
            }

            // Create new lock
            SlotLock newLock = new SlotLock();
            newLock.setStylistId(request.getStylistId());
            newLock.setSlotDate(date);
            newLock.setSlotTime(time);
            newLock.setDurationMinutes(request.getDurationMinutes());
            newLock.setSessionId(request.getSessionId());
            newLock.setUserEmail(userEmail);
            newLock.setLockedAt(OffsetDateTime.now());
            newLock.setExpiresAt(OffsetDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            newLock.setIsActive(true);

            SlotLock savedLock = slotLockRepository.save(newLock);

            response.setLockId(savedLock.getId());
            response.setSessionId(savedLock.getSessionId());
            response.setExpiresAt(savedLock.getExpiresAt().toString());
            response.setSecondsRemaining(LOCK_DURATION_MINUTES * 60);
            response.setSuccess(true);
            response.setMessage("Slot locked successfully for 3 minutes");

            System.out.println("✅ Slot locked: " + request.getStylistId() +
                    " | " + date + " " + time + " | Session: " + request.getSessionId());

            return response;

        } catch (Exception e) {
            System.err.println("❌ Error locking slot: " + e.getMessage());
            response.setSuccess(false);
            response.setMessage("Failed to lock slot: " + e.getMessage());
            return response;
        }
    }

    /**
     * Release all locks for a session
     * Called when booking is completed or cancelled
     */
    @Transactional
    public void releaseLocks(String sessionId) {
        try {
            List<SlotLock> locks = slotLockRepository.findBySessionIdAndIsActiveTrue(sessionId);

            for (SlotLock lock : locks) {
                lock.setIsActive(false);
                slotLockRepository.save(lock);
            }

            if (!locks.isEmpty()) {
                System.out.println("✅ Released " + locks.size() + " locks for session: " + sessionId);
            }

        } catch (Exception e) {
            System.err.println("❌ Error releasing locks: " + e.getMessage());
        }
    }

    /**
     * Check if a slot is currently locked
     */
    public boolean isSlotLocked(Long stylistId, LocalDate date, LocalTime time) {
        Optional<SlotLock> lock = slotLockRepository.findActiveLock(stylistId, date, time);

        if (lock.isPresent()) {
            if (lock.get().isExpired()) {
                // Deactivate expired lock
                SlotLock expiredLock = lock.get();
                expiredLock.setIsActive(false);
                slotLockRepository.save(expiredLock);
                return false;
            }
            return true;
        }

        return false;
    }

    /**
     * Extend lock expiration for a session
     * Called periodically while user is on booking page
     */
    @Transactional
    public void extendLocks(String sessionId) {
        try {
            List<SlotLock> locks = slotLockRepository.findBySessionIdAndIsActiveTrue(sessionId);

            for (SlotLock lock : locks) {
                lock.setExpiresAt(OffsetDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                slotLockRepository.save(lock);
            }

            if (!locks.isEmpty()) {
                System.out.println(" Extended " + locks.size() + " locks for session: " + sessionId);
            }

        } catch (Exception e) {
            System.err.println(" Error extending locks: " + e.getMessage());
        }
    }

    /**
     * Scheduled task: Cleanup expired locks every 1 minute
     * Runs automatically in the background
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    @Transactional
    public void cleanupExpiredLocks() {
        try {
            OffsetDateTime now = OffsetDateTime.now();
            List<SlotLock> expiredLocks = slotLockRepository
                    .findByIsActiveTrueAndExpiresAtBefore(now);

            for (SlotLock lock : expiredLocks) {
                lock.setIsActive(false);
                slotLockRepository.save(lock);
            }

            if (!expiredLocks.isEmpty()) {
                System.out.println("🧹 Cleaned up " + expiredLocks.size() + " expired slot locks");
            }

        } catch (Exception e) {
            System.err.println("git  Error in cleanup task: " + e.getMessage());
        }
    }

    /**
     * Get all active locks for a specific date (admin view)
     */
    public List<SlotLock> getActiveLocksForDate(Long stylistId, LocalDate date) {
        return slotLockRepository.findByStylistAndDate(stylistId, date);
    }

    /**
     * Force release a specific lock (admin only)
     */
    @Transactional
    public void forceReleaseLock(Long lockId) {
        Optional<SlotLock> lock = slotLockRepository.findById(lockId);

        if (lock.isPresent()) {
            SlotLock slotLock = lock.get();
            slotLock.setIsActive(false);
            slotLockRepository.save(slotLock);
            System.out.println("Force released lock: " + lockId);
        }
    }
}