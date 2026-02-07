package com.saloon.saloon_backend.service;

import com.saloon.saloon_backend.dto.AppointmentCreateRequest;
import com.saloon.saloon_backend.dto.AppointmentCreateResponse;
import com.saloon.saloon_backend.entity.Appointment;
import com.saloon.saloon_backend.entity.SalonService;  // Updated
import com.saloon.saloon_backend.entity.User;
import com.saloon.saloon_backend.repository.AppointmentRepository;
import com.saloon.saloon_backend.repository.SalonServiceRepository;  // Updated
import com.saloon.saloon_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final SalonServiceRepository salonServiceRepository;  // Updated

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            UserRepository userRepository,
            SalonServiceRepository salonServiceRepository  // Updated
    ) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.salonServiceRepository = salonServiceRepository;  // Updated
    }

    public AppointmentCreateResponse createAppointment(String clientEmail, AppointmentCreateRequest req) {

        if (req.stylistId == null) throw new IllegalArgumentException("stylistId is required");
        if (req.startTs == null || req.startTs.isBlank()) throw new IllegalArgumentException("startTs is required");
        if (req.serviceIds == null || req.serviceIds.isEmpty()) throw new IllegalArgumentException("serviceIds is required");

        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        User stylist = userRepository.findById(req.stylistId)
                .orElseThrow(() -> new IllegalArgumentException("Stylist not found"));

        OffsetDateTime start = OffsetDateTime.parse(req.startTs);

        List<SalonService> services = salonServiceRepository.findByIdIn(req.serviceIds);  // Updated
        if (services.size() != req.serviceIds.size()) {
            throw new IllegalArgumentException("One or more serviceIds are invalid");
        }

        int totalDurationMin = services.stream()
                .mapToInt(SalonService::getDefaultDurationMin)  // Updated
                .sum();

        // TODO later: add stylist buffer_min from stylist_profiles
        OffsetDateTime end = start.plusMinutes(totalDurationMin);

        // Overlap check
        boolean overlap = appointmentRepository.existsOverlap(stylist.getId(), start, end);
        if (overlap) {
            throw new IllegalStateException("Selected time overlaps with another booking");
        }

        Appointment appt = new Appointment();
        appt.setClient(client);
        appt.setStylist(stylist);
        appt.setStartTs(start);
        appt.setEndTs(end);
        appt.setStatus("BOOKED");

        Appointment saved = appointmentRepository.save(appt);

        return new AppointmentCreateResponse(
                saved.getId(),
                saved.getStartTs().toString(),
                saved.getEndTs().toString(),
                saved.getStatus()
        );
    }
}
