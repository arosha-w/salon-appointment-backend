package com.saloon.saloon_backend.service;

import com.saloon.saloon_backend.dto.*;
import com.saloon.saloon_backend.entity.Appointment;
import com.saloon.saloon_backend.entity.AppointmentItem;
import com.saloon.saloon_backend.entity.User;
import com.saloon.saloon_backend.repository.AppointmentRepository;
import com.saloon.saloon_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    public ClientService(UserRepository userRepository, AppointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public ClientProfileDTO getClientProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ClientProfileDTO dto = new ClientProfileDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());

        // Split full name into first and last name
        String[] nameParts = user.getFullName().split(" ", 2);
        dto.setFirstName(nameParts[0]);
        dto.setLastName(nameParts.length > 1 ? nameParts[1] : "");

        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());

        // Format member since
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        dto.setMemberSince(user.getCreatedAt().format(formatter));

        // Get stats
        List<Appointment> completedAppointments = appointmentRepository
                .findByClientIdAndStatus(user.getId(), "COMPLETED");
        dto.setTotalVisits(completedAppointments.size());

        // Calculate loyalty points (example: 10 points per visit)
        dto.setLoyaltyPoints(completedAppointments.size() * 10);

        // Determine membership tier
        if (dto.getTotalVisits() >= 50) {
            dto.setMembershipTier("VIP");
        } else if (dto.getTotalVisits() >= 20) {
            dto.setMembershipTier("Gold");
        } else {
            dto.setMembershipTier("Regular");
        }

        return dto;
    }

    @Transactional
    public ClientProfileDTO updateClientProfile(String email, ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update user details
        if (request.getFirstName() != null && request.getLastName() != null) {
            user.setFullName(request.getFirstName() + " " + request.getLastName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        // Note: Email updates might require additional verification
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        userRepository.save(user);

        return getClientProfile(user.getEmail());
    }

    @Transactional
    public ClientStatsDTO getClientStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Appointment> completedAppointments = appointmentRepository
                .findByClientIdAndStatus(user.getId(), "COMPLETED");

        Integer totalVisits = completedAppointments.size();

        BigDecimal totalSpent = completedAppointments.stream()
                .map(Appointment::getTotalPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer loyaltyPoints = totalVisits * 10; // 10 points per visit

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        String memberSince = user.getCreatedAt().format(formatter);

        return new ClientStatsDTO(totalVisits, totalSpent, loyaltyPoints, memberSince);
    }

    @Transactional
    public List<AppointmentHistoryDTO> getAppointmentHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Appointment> appointments = appointmentRepository
                .findPastByClientId(user.getId(), OffsetDateTime.now());

        return appointments.stream()
                .map(this::mapToHistoryDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    private AppointmentHistoryDTO mapToHistoryDTO(Appointment appointment) {
        AppointmentHistoryDTO dto = new AppointmentHistoryDTO();
        dto.setId(appointment.getId());
        dto.setDate(appointment.getStartTs());
        dto.setStylistName(appointment.getStylist().getFullName());
        dto.setAmount(appointment.getTotalPrice());
        dto.setStatus(appointment.getStatus());

        // Get service names from appointment items
        List<String> serviceNames = appointment.getItems().stream()
                .map(item -> item.getService().getName())
                .collect(Collectors.toList());

        dto.setServiceNames(serviceNames);
        dto.setServiceName(String.join(", ", serviceNames));

        return dto;
    }
}