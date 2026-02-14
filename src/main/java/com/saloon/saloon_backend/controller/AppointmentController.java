package com.saloon.saloon_backend.controller;

import com.saloon.saloon_backend.dto.*;
import com.saloon.saloon_backend.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:3000")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
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
    public ResponseEntity<Void> cancelAppointment(
            @PathVariable Long id,
            Authentication authentication) {

        String clientEmail = authentication.getName();
        appointmentService.cancelAppointment(id, clientEmail);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/slots")
    public ResponseEntity<List<AvailableSlotDTO>> getAvailableSlots(
            @RequestParam Long stylistId,
            @RequestParam String date,
            @RequestParam(required = false) Integer durationMin) {

        List<AvailableSlotDTO> slots = appointmentService.getAvailableSlots(stylistId, date, durationMin);
        return ResponseEntity.ok(slots);
    }
}