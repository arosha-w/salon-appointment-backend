package com.saloon.saloon_backend.controller;

import com.saloon.saloon_backend.dto.BestTimeToBookDTO;
import com.saloon.saloon_backend.dto.ClientAppointmentHistoryDTO;
import com.saloon.saloon_backend.dto.StylistDTO;
import com.saloon.saloon_backend.dto.TimeSlotAvailabilityDTO;
import com.saloon.saloon_backend.entity.Appointment;
import com.saloon.saloon_backend.entity.StylistProfile;
import com.saloon.saloon_backend.entity.User;
import com.saloon.saloon_backend.repository.AppointmentRepository;
import com.saloon.saloon_backend.repository.StylistProfileRepository;
import com.saloon.saloon_backend.repository.UserRepository;
import com.saloon.saloon_backend.service.ClientBookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ClientController {

    private final StylistProfileRepository stylistProfileRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClientBookingService clientBookingService;

    public ClientController(
            StylistProfileRepository stylistProfileRepository,
            UserRepository userRepository,
            AppointmentRepository appointmentRepository,
            ClientBookingService clientBookingService
    ) {
        this.stylistProfileRepository = stylistProfileRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.clientBookingService = clientBookingService;
    }

    @Transactional
    @GetMapping("/booking/slots-with-demand")
    public ResponseEntity<List<TimeSlotAvailabilityDTO>> getSlotsWithDemand(
            @RequestParam(required = false) Long stylistId,
            @RequestParam String date,
            @RequestParam Integer durationMin
    ) {
        List<TimeSlotAvailabilityDTO> slots = clientBookingService
                .getAvailableSlotsWithDemand(stylistId, date, durationMin);
        return ResponseEntity.ok(slots);
    }

    /**
     * Get best times to book (next 7 days)
     */
    @Transactional
    @GetMapping("/booking/best-times")
    public ResponseEntity<List<BestTimeToBookDTO>> getBestTimesToBook() {
        return ResponseEntity.ok(clientBookingService.getBestTimesToBook());
    }

    /**
     * Get peak hours information
     */
    @Transactional
    @GetMapping("/booking/peak-hours-info")
    public ResponseEntity<Map<String, Object>> getPeakHoursInfo() {
        return ResponseEntity.ok(clientBookingService.getPeakHoursInfo());
    }

    // ✅ PUBLIC: anyone can fetch stylists (NO token required)
    @Transactional
    @GetMapping("/stylists")
    public ResponseEntity<List<StylistDTO>> getAllStylists() {
        List<StylistProfile> profiles = stylistProfileRepository.findAvailableStylists();

        List<StylistDTO> dtos = profiles.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // ✅ CLIENT ONLY: Get client stats
    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/client/stats")
    @Transactional
    public ResponseEntity<Map<String, Object>> getClientStats(Authentication auth) {
        String email = auth.getName();
        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        // Get all completed appointments for this client
        List<Appointment> completedAppointments = appointmentRepository.findByClientId(client.getId())
                .stream()
                .filter(a -> "COMPLETED".equals(a.getStatus()))
                .collect(Collectors.toList());

        // Calculate stats
        int totalVisits = completedAppointments.size();

        BigDecimal totalSpent = completedAppointments.stream()
                .map(Appointment::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Simple loyalty points calculation (1 point per dollar spent)
        int loyaltyPoints = totalSpent.intValue();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVisits", totalVisits);
        stats.put("totalSpent", totalSpent.doubleValue());
        stats.put("loyaltyPoints", loyaltyPoints);
        stats.put("memberSince", client.getCreatedAt().toLocalDate().toString());

        return ResponseEntity.ok(stats);
    }

    /**
     * ✅ FIXED: Get appointment history (COMPLETED + CANCELLED appointments)
     * This shows all past appointments regardless of status
     */
    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/client/history")
    @Transactional
    public ResponseEntity<List<ClientAppointmentHistoryDTO>> getAppointmentHistory(Authentication auth) {
        String email = auth.getName();
        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        // ✅ FIX: Get COMPLETED AND CANCELLED appointments
        List<Appointment> historyAppointments = appointmentRepository.findByClientId(client.getId())
                .stream()
                .filter(a ->
                        "COMPLETED".equals(a.getStatus()) ||
                                "CANCELLED".equals(a.getStatus())
                )
                .sorted((a, b) -> b.getStartTs().compareTo(a.getStartTs())) // Newest first
                .collect(Collectors.toList());

        List<ClientAppointmentHistoryDTO> history = historyAppointments.stream()
                .map(this::mapToHistoryDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(history);
    }

    // ✅ CLIENT ONLY: Get client profile
    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/client/profile")
    public ResponseEntity<Map<String, Object>> getClientProfile(Authentication auth) {
        String email = auth.getName();
        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        Map<String, Object> profile = new HashMap<>();
        profile.put("email", email);
        profile.put("name", client.getFullName());
        profile.put("phone", client.getPhone());

        return ResponseEntity.ok(profile);
    }

    // -------------------------
    // Helper mappers
    // -------------------------

    @Transactional
    private StylistDTO mapToDTO(StylistProfile profile) {
        StylistDTO dto = new StylistDTO();
        dto.setId(profile.getUser().getId());
        dto.setName(profile.getUser().getFullName());
        dto.setEmail(profile.getUser().getEmail());
        dto.setSpecialties(profile.getSpecialties());
        dto.setExperienceYears(profile.getExperienceYears());
        dto.setBio(profile.getBio());
        dto.setRating(profile.getRating());
        dto.setTotalReviews(profile.getTotalReviews());
        dto.setIsAvailable(profile.getIsAvailable());
        return dto;
    }

    @Transactional
    private ClientAppointmentHistoryDTO mapToHistoryDTO(Appointment appointment) {
        ClientAppointmentHistoryDTO dto = new ClientAppointmentHistoryDTO();
        dto.setId(appointment.getId());
        dto.setDate(appointment.getStartTs().toString());
        dto.setStylistName(appointment.getStylist().getFullName());
        dto.setStylistId(appointment.getStylist().getId()); // ✅ Add stylistId for rebook
        dto.setAmount(appointment.getTotalPrice().doubleValue());
        dto.setStatus(appointment.getStatus());

        // Get service names from appointment items
        String serviceName = appointment.getItems().stream()
                .map(item -> item.getService().getName())
                .collect(Collectors.joining(", "));
        dto.setServiceName(serviceName);

        return dto;
    }
}