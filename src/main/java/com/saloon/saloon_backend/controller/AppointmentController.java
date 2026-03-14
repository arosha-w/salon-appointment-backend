package com.saloon.saloon_backend.controller;

import com.saloon.saloon_backend.dto.*;
import com.saloon.saloon_backend.entity.Appointment;
import com.saloon.saloon_backend.entity.User;
import com.saloon.saloon_backend.repository.AppointmentRepository;
import com.saloon.saloon_backend.repository.UserRepository;
import com.saloon.saloon_backend.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:3000")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public AppointmentController(
            AppointmentService appointmentService,
            AppointmentRepository appointmentRepository,
            UserRepository userRepository
    ) {
        this.appointmentService = appointmentService;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<AppointmentCreateResponse> createAppointment(
            Authentication authentication,
            @RequestBody AppointmentCreateRequest req) {

        String clientEmail = authentication.getName();
        AppointmentCreateResponse response = appointmentService.createAppointment(clientEmail, req);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<AppointmentDTO>> getMyAppointments(Authentication authentication) {
        String clientEmail = authentication.getName();
        List<AppointmentDTO> appointments = appointmentService.getClientAppointments(clientEmail);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<AppointmentDTO>> getUpcomingAppointments(Authentication authentication) {
        String clientEmail = authentication.getName();
        List<AppointmentDTO> appointments = appointmentService.getUpcomingAppointments(clientEmail);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/past")
    public ResponseEntity<List<AppointmentDTO>> getPastAppointments(Authentication authentication) {
        String clientEmail = authentication.getName();
        List<AppointmentDTO> appointments = appointmentService.getPastAppointments(clientEmail);
        return ResponseEntity.ok(appointments);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String clientEmail = authentication.getName();
            appointmentService.cancelAppointment(id, clientEmail);
            return ResponseEntity.ok(Map.of("message", "Appointment cancelled"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/slots")
    public ResponseEntity<List<AvailableSlotDTO>> getAvailableSlots(
            @RequestParam Long stylistId,
            @RequestParam String date,
            @RequestParam(required = false) Integer durationMin) {

        List<AvailableSlotDTO> slots = appointmentService.getAvailableSlots(stylistId, date, durationMin);
        return ResponseEntity.ok(slots);
    }

    /**
     * Get available slots for reschedule modal
     * GET /api/appointments/available-slots?stylistId=2&date=2026-03-10&durationMin=60
     */
    @GetMapping("/available-slots")
    public ResponseEntity<?> getAvailableSlotsForReschedule(
            @RequestParam Long stylistId,
            @RequestParam String date,
            @RequestParam Integer durationMin
    ) {
        try {
            LocalDate requestDate = LocalDate.parse(date);

            userRepository.findById(stylistId)
                    .orElseThrow(() -> new IllegalArgumentException("Stylist not found"));

            ZoneOffset sriLankaOffset = ZoneOffset.ofHoursMinutes(5, 30);

            List<Map<String, Object>> slots = new ArrayList<>();

            for (int hour = 9; hour <= 18; hour++) {
                LocalTime slotTime = LocalTime.of(hour, 0);

                OffsetDateTime slotStart = OffsetDateTime.of(requestDate, slotTime, sriLankaOffset);
                OffsetDateTime slotEnd = slotStart.plusMinutes(durationMin);

                boolean isAvailable = isSlotAvailable(stylistId, slotStart, slotEnd);

                if (isAvailable) {
                    Map<String, Object> slot = new HashMap<>();
                    slot.put("time", String.format("%02d:00", hour));
                    slot.put("isAvailable", true);
                    slots.add(slot);
                }
            }

            return ResponseEntity.ok(slots);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error generating slots: " + e.getMessage()));
        }
    }

    private boolean isSlotAvailable(Long stylistId, OffsetDateTime start, OffsetDateTime end) {
        List<Appointment> overlapping = appointmentRepository.findAll().stream()
                .filter(apt -> apt.getStylist().getId().equals(stylistId))
                .filter(apt -> {
                    boolean startsBeforeEnd = apt.getStartTs().isBefore(end);
                    boolean endsAfterStart = apt.getEndTs().isAfter(start);
                    return startsBeforeEnd && endsAfterStart;
                })
                .filter(apt -> !"CANCELLED".equals(apt.getStatus()))
                .collect(Collectors.toList());

        return overlapping.isEmpty();
    }

    /**
     * ✅ FIXED: Reschedule an appointment
     * PUT /api/appointments/{id}/reschedule
     *
     * IMPORTANT FIX:
     * - Accept BOTH "CLIENT" and "ROLE_CLIENT"
     * - Return JSON message so frontend shows real reason (not blank)
     */
    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyAuthority('CLIENT','ROLE_CLIENT')")
    public ResponseEntity<?> rescheduleAppointment(
            @PathVariable Long id,
            @RequestBody RescheduleRequestDTO request,
            Authentication auth
    ) {
        try {
            if (auth == null || auth.getName() == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));
            }

            String email = auth.getName();
            appointmentService.rescheduleAppointment(id, email, request);

            return ResponseEntity.ok(Map.of("message", "Appointment rescheduled successfully"));

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Server error: " + e.getMessage()));
        }
    }
}