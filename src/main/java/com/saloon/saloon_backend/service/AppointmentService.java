package com.saloon.saloon_backend.service;

import com.saloon.saloon_backend.dto.*;
import com.saloon.saloon_backend.entity.*;
import com.saloon.saloon_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final SalonServiceRepository salonServiceRepository;
    private final StylistProfileRepository stylistProfileRepository;
    private final AppointmentItemRepository appointmentItemRepository;

    private static final ZoneId SALON_TIMEZONE = ZoneId.of("Asia/Colombo");

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            UserRepository userRepository,
            SalonServiceRepository salonServiceRepository,
            StylistProfileRepository stylistProfileRepository,
            AppointmentItemRepository appointmentItemRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.salonServiceRepository = salonServiceRepository;
        this.stylistProfileRepository = stylistProfileRepository;
        this.appointmentItemRepository = appointmentItemRepository;
    }

    /**
     * Get available time slots for a stylist on a specific date
     */
    @Transactional
    public List<AvailableSlotDTO> getAvailableSlots(Long stylistId, String date, Integer durationMin) {
        // Parse the date
        LocalDate requestedDate = LocalDate.parse(date);

        // Get start and end of the day in salon timezone
        OffsetDateTime dayStart = requestedDate.atStartOfDay(SALON_TIMEZONE).toOffsetDateTime();
        OffsetDateTime dayEnd = dayStart.plusDays(1);

        // Get existing appointments for this stylist on this day
        List<Appointment> existingAppointments = appointmentRepository
                .findByStylistAndDateRange(stylistId, dayStart, dayEnd);

        // Get stylist buffer time
        StylistProfile profile = stylistProfileRepository.findByUserId(stylistId).orElse(null);
        int bufferMinutes = (profile != null) ? profile.getBufferMinutes() : 10;

        // Generate time slots (9 AM to 6 PM, every 30 minutes)
        List<AvailableSlotDTO> slots = new ArrayList<>();
        OffsetDateTime slotTime = dayStart.withHour(9).withMinute(0);
        OffsetDateTime endOfDay = dayStart.withHour(18).withMinute(0);

        while (slotTime.isBefore(endOfDay)) {
            OffsetDateTime slotEnd = slotTime.plusMinutes(durationMin + bufferMinutes);

            // Check if this slot overlaps with any existing appointment
            boolean isAvailable = true;
            for (Appointment appointment : existingAppointments) {
                if (slotTime.isBefore(appointment.getEndTs()) &&
                        slotEnd.isAfter(appointment.getStartTs())) {
                    isAvailable = false;
                    break;
                }
            }

            // Create slot DTO
            AvailableSlotDTO slot = new AvailableSlotDTO();
            slot.setTime(formatDisplayTime(slotTime));
            slot.setTimestamp(slotTime.toString());
            slot.setAvailable(isAvailable);

            slots.add(slot);

            slotTime = slotTime.plusMinutes(30);
        }

        return slots;
    }

    /**
     * Format time for display (e.g., "2:30 PM")
     */
    @Transactional
    private String formatDisplayTime(OffsetDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("h:mm a"));
    }

    /**
     * Create a new appointment
     */
    @Transactional
    public AppointmentCreateResponse createAppointment(String clientEmail, AppointmentCreateRequest req) {
        // Get client
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        // Get stylist
        User stylist = userRepository.findById(req.getStylistId())
                .orElseThrow(() -> new IllegalArgumentException("Stylist not found"));

        // Parse start time
        OffsetDateTime start = OffsetDateTime.parse(req.getStartTs());

        // Get services
        List<SalonService> services = salonServiceRepository.findByIdIn(req.getServiceIds());
        if (services.size() != req.getServiceIds().size()) {
            throw new IllegalArgumentException("One or more services not found");
        }



        // Calculate total duration and price
        int totalDurationMin = services.stream()
                .mapToInt(SalonService::getDefaultDurationMin)
                .sum();

        BigDecimal totalPrice = services.stream()
                .map(SalonService::getBasePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get buffer time
        StylistProfile profile = stylistProfileRepository.findByUserId(stylist.getId()).orElse(null);
        int bufferMin = (profile != null) ? profile.getBufferMinutes() : 10;

        // Calculate end time
        OffsetDateTime end = start.plusMinutes(totalDurationMin + bufferMin);

        // Check for overlaps
        boolean overlap = appointmentRepository.existsOverlap(stylist.getId(), start, end);
        if (overlap) {
            throw new IllegalStateException("Time slot not available");
        }

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setStylist(stylist);
        appointment.setStartTs(start);
        appointment.setEndTs(end);
        appointment.setStatus("BOOKED"); // ✅ CHANGED: Start as BOOKED instead of CONFIRMED
        appointment.setTotalPrice(totalPrice);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Create appointment items
        for (SalonService service : services) {
            AppointmentItem item = new AppointmentItem();
            item.setAppointment(savedAppointment);
            item.setService(service);
            item.setDurationMin(service.getDefaultDurationMin());
            item.setPrice(service.getBasePrice());
            appointmentItemRepository.save(item);
        }

        return new AppointmentCreateResponse(
                savedAppointment.getId(),
                savedAppointment.getStartTs().toString(),
                savedAppointment.getEndTs().toString(),
                "BOOKED" // ✅ Return BOOKED status
        );
    }

    /**
     * Get all appointments for a client
     */
    @Transactional
    public List<AppointmentDTO> getClientAppointments(String clientEmail) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        List<Appointment> appointments = appointmentRepository.findByClientId(client.getId());

        return appointments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming appointments for a client
     */
    @Transactional
    public List<AppointmentDTO> getUpcomingAppointments(String clientEmail) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        List<Appointment> appointments = appointmentRepository
                .findUpcomingByClientId(client.getId(), OffsetDateTime.now(SALON_TIMEZONE));

        return appointments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get past appointments for a client
     */
    @Transactional
    public List<AppointmentDTO> getPastAppointments(String clientEmail) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        List<Appointment> appointments = appointmentRepository
                .findPastByClientId(client.getId(), OffsetDateTime.now(SALON_TIMEZONE));

        return appointments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cancel an appointment
     */
    @Transactional
    public void cancelAppointment(Long appointmentId, String clientEmail) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appointment.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("You can only cancel your own appointments");
        }

        // Check 24-hour cancellation policy
        if (appointment.getStartTs().isBefore(OffsetDateTime.now(SALON_TIMEZONE).plusHours(24))) {
            throw new IllegalStateException("Cannot cancel appointments less than 24 hours before start time");
        }

        appointment.setStatus("CANCELLED");
        appointmentRepository.save(appointment);
    }
    // Add reschedule method

    @Transactional
    public void rescheduleAppointment(Long appointmentId, String clientEmail, RescheduleRequestDTO request) {
        // 1. Find appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        // 2. Verify ownership - only the client who booked can reschedule
        if (!appointment.getClient().getEmail().equals(clientEmail)) {
            throw new IllegalArgumentException("You can only reschedule your own appointments");
        }

        // 3. Parse new start time
        OffsetDateTime newStartTs = OffsetDateTime.parse(request.getNewStartTs());

        // 4. Calculate duration from original appointment
        long durationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(
                appointment.getStartTs(),
                appointment.getEndTs()
        );
        OffsetDateTime newEndTs = newStartTs.plusMinutes(durationMinutes);

        // 5. Update stylist if provided
        if (request.getNewStylistId() != null && !request.getNewStylistId().equals(appointment.getStylist().getId())) {
            User newStylist = userRepository.findById(request.getNewStylistId())
                    .orElseThrow(() -> new IllegalArgumentException("Stylist not found"));
            appointment.setStylist(newStylist);
        }

        // 6. Update appointment times
        appointment.setStartTs(newStartTs);
        appointment.setEndTs(newEndTs);

        // 7. Reset status to BOOKED (needs confirmation)
        appointment.setStatus("BOOKED");

        // 8. Save
        appointmentRepository.save(appointment);

        System.out.println("✅ Appointment " + appointmentId + " rescheduled to " + newStartTs);
    }

    /**
     * Map Appointment entity to DTO
     */
    @Transactional
    private AppointmentDTO mapToDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setClientId(appointment.getClient().getId());
        dto.setClientName(appointment.getClient().getFullName());
        dto.setStylistId(appointment.getStylist().getId());
        dto.setStylistName(appointment.getStylist().getFullName());
        dto.setStartTs(appointment.getStartTs());
        dto.setEndTs(appointment.getEndTs());
        dto.setStatus(appointment.getStatus());
        dto.setTotalPrice(appointment.getTotalPrice());
        dto.setNotes(appointment.getNotes());
        dto.setCreatedAt(appointment.getCreatedAt());

        // Map appointment items
        List<AppointmentItemDTO> items = appointment.getItems().stream()
                .map(item -> {
                    AppointmentItemDTO itemDTO = new AppointmentItemDTO();
                    itemDTO.setServiceId(item.getService().getId());
                    itemDTO.setServiceName(item.getService().getName());
                    itemDTO.setDurationMin(item.getDurationMin());
                    itemDTO.setPrice(item.getPrice());
                    return itemDTO;
                })
                .collect(Collectors.toList());
        dto.setItems(items);

        return dto;
    }
}