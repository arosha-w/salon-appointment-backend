package com.saloon.saloon_backend.controller;

import com.saloon.saloon_backend.dto.*;
import com.saloon.saloon_backend.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/smart-scheduling")
@CrossOrigin(origins = "http://localhost:3000")
public class SmartSchedulingController {

    private final SlotLockService slotLockService;
    private final SmartSchedulingService smartSchedulingService;
    private final IdleCapacityService idleCapacityService;

    public SmartSchedulingController(
            SlotLockService slotLockService,
            SmartSchedulingService smartSchedulingService,
            IdleCapacityService idleCapacityService
    ) {
        this.slotLockService = slotLockService;
        this.smartSchedulingService = smartSchedulingService;
        this.idleCapacityService = idleCapacityService;
    }

    /**
     * Lock a slot temporarily (5 minutes)
     * Called when user selects a slot during booking
     */
    @PostMapping("/lock-slot")
    public ResponseEntity<SlotLockResponseDTO> lockSlot(
            @RequestBody SlotLockRequestDTO request,
            Authentication auth
    ) {
        String userEmail = auth != null ? auth.getName() : null;
        SlotLockResponseDTO response = slotLockService.lockSlot(request, userEmail);
        return ResponseEntity.ok(response);
    }

    /**
     * Release locks for a session
     * Called when booking is completed or cancelled
     */
    @PostMapping("/release-locks")
    public ResponseEntity<Void> releaseLocks(@RequestParam String sessionId) {
        slotLockService.releaseLocks(sessionId);
        return ResponseEntity.ok().build();
    }

    /**
     * Extend lock duration
     * Called periodically while user is on booking page
     */
    @PostMapping("/extend-locks")
    public ResponseEntity<Void> extendLocks(@RequestParam String sessionId) {
        slotLockService.extendLocks(sessionId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get smart slot recommendations for rescheduling
     * Returns AI-powered suggestions
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<SmartSlotRecommendationDTO>> getSmartRecommendations(
            @RequestParam Long appointmentId,
            @RequestParam(required = false) String preferredDate
    ) {
        LocalDate date = preferredDate != null ? LocalDate.parse(preferredDate) : null;
        List<SmartSlotRecommendationDTO> recommendations =
                smartSchedulingService.getSmartRecommendations(appointmentId, date);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Get idle capacity alerts (Admin only)
     */
    @GetMapping("/idle-capacity/alerts")
    public ResponseEntity<List<IdleCapacityAlertDTO>> getIdleCapacityAlerts() {
        List<IdleCapacityAlertDTO> alerts = idleCapacityService.getUnresolvedAlerts();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get idle capacity alerts for date range
     */
    @GetMapping("/idle-capacity/alerts/range")
    public ResponseEntity<List<IdleCapacityAlertDTO>> getAlertsForDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<IdleCapacityAlertDTO> alerts =
                idleCapacityService.getAlertsForDateRange(start, end);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Resolve an idle capacity alert
     */
    @PutMapping("/idle-capacity/alerts/{id}/resolve")
    public ResponseEntity<Void> resolveAlert(@PathVariable Long id) {
        idleCapacityService.resolveAlert(id);
        return ResponseEntity.ok().build();
    }
}










































































































































































































































