package com.saloon.saloon_backend.controller;

import com.saloon.saloon_backend.dto.StylistAppointmentDTO;
import com.saloon.saloon_backend.dto.StylistDashboardStatsDTO;
import com.saloon.saloon_backend.repository.StylistProfileRepository;
import com.saloon.saloon_backend.service.StylistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stylist")
@PreAuthorize("hasRole('STYLIST')")
public class StylistController {

    private final StylistService stylistService;

    public StylistController(StylistService stylistService, StylistProfileRepository stylistProfileRepository) {
        this.stylistService = stylistService;
        this.stylistProfileRepository = stylistProfileRepository;
    }

    private final StylistProfileRepository stylistProfileRepository;

    /**
     * Get dashboard statistics
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<StylistDashboardStatsDTO> getDashboardStats(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(stylistService.getDashboardStats(email));
    }

    /**
     * Get all appointments
     */
    @GetMapping("/appointments")
    public ResponseEntity<List<StylistAppointmentDTO>> getAppointments(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(stylistService.getAppointments(email));
    }

    /**
     * Get today's appointments
     */
    @GetMapping("/appointments/today")
    public ResponseEntity<List<StylistAppointmentDTO>> getTodayAppointments(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(stylistService.getTodayAppointments(email));
    }

    /**
     * Get pending confirmations
     */
    @GetMapping("/appointments/pending")
    public ResponseEntity<List<StylistAppointmentDTO>> getPendingConfirmations(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(stylistService.getPendingConfirmations(email));
    }

    /**
     * Confirm appointment
     */
    @PutMapping("/appointments/{id}/confirm")
    public ResponseEntity<Void> confirmAppointment(@PathVariable Long id, Authentication auth) {
        String email = auth.getName();
        stylistService.confirmAppointment(id, email);
        return ResponseEntity.ok().build();
    }

    /**
     * Complete appointment
     */
    @PutMapping("/appointments/{id}/complete")
    public ResponseEntity<Void> completeAppointment(@PathVariable Long id, Authentication auth) {
        String email = auth.getName();
        stylistService.completeAppointment(id, email);
        return ResponseEntity.ok().build();
    }

    /**
     * Mark no-show
     */
    @PutMapping("/appointments/{id}/no-show")
    public ResponseEntity<Void> markNoShow(@PathVariable Long id, Authentication auth) {
        String email = auth.getName();
        stylistService.markNoShow(id, email);
        return ResponseEntity.ok().build();
    }

    /**
     * Cancel appointment
     */
    @PutMapping("/appointments/{id}/cancel")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id, Authentication auth) {
        String email = auth.getName();
        stylistService.cancelAppointment(id, email);
        return ResponseEntity.ok().build();
    }
}
