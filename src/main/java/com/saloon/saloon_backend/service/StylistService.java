package com.saloon.saloon_backend.service;

import com.saloon.saloon_backend.dto.StylistAppointmentDTO;
import com.saloon.saloon_backend.dto.StylistDashboardStatsDTO;
import com.saloon.saloon_backend.entity.Appointment;
import com.saloon.saloon_backend.entity.User;
import com.saloon.saloon_backend.repository.AppointmentRepository;
import com.saloon.saloon_backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StylistService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private static final ZoneId SALON_TIMEZONE = ZoneId.of("Asia/Colombo");

    public StylistService(
            AppointmentRepository appointmentRepository,
            UserRepository userRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get dashboard statistics for stylist
     */
    public StylistDashboardStatsDTO getDashboardStats(String stylistEmail) {
        User stylist = userRepository.findByEmail(stylistEmail)
                .orElseThrow(() -> new IllegalArgumentException("Stylist not found"));

        OffsetDateTime todayStart = LocalDate.now(SALON_TIMEZONE)
                .atStartOfDay(SALON_TIMEZONE).toOffsetDateTime();
        OffsetDateTime todayEnd = todayStart.plusDays(1);
        OffsetDateTime weekStart = todayStart.minusDays(7);

        // Get today's appointments
        List<Appointment> todayAppointments = appointmentRepository
                .findByStylistAndDateRange(stylist.getId(), todayStart, todayEnd);

        // Count by status
        int pending = (int) todayAppointments.stream()
                .filter(a -> "BOOKED".equals(a.getStatus()))
                .count();

        int completed = (int) todayAppointments.stream()
                .filter(a -> "COMPLETED".equals(a.getStatus()))
                .count();

        // Calculate today's earnings
        BigDecimal earningsToday = todayAppointments.stream()
                .filter(a -> "COMPLETED".equals(a.getStatus()))
                .map(Appointment::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get weekly appointments
        List<Appointment> weekAppointments = appointmentRepository
                .findByStylistAndDateRange(stylist.getId(), weekStart, todayEnd);

        BigDecimal weeklyEarnings = weekAppointments.stream()
                .filter(a -> "COMPLETED".equals(a.getStatus()))
                .map(Appointment::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get unique clients count
        int totalClients = (int) appointmentRepository
                .findByStylistAndDateRange(stylist.getId(), weekStart, todayEnd)
                .stream()
                .map(a -> a.getClient().getId())
                .distinct()
                .count();

        return new StylistDashboardStatsDTO(
                todayAppointments.size(),
                pending,
                completed,
                earningsToday,
                totalClients,
                weeklyEarnings
        );
    }

    /**
     * Get all appointments for stylist
     */
    public List<StylistAppointmentDTO> getAppointments(String stylistEmail) {
        User stylist = userRepository.findByEmail(stylistEmail)
                .orElseThrow(() -> new IllegalArgumentException("Stylist not found"));

        OffsetDateTime now = OffsetDateTime.now(SALON_TIMEZONE);
        OffsetDateTime futureLimit = now.plusDays(30);

        List<Appointment> appointments = appointmentRepository
                .findByStylistAndDateRange(stylist.getId(), now.minusDays(7), futureLimit);

        return appointments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get today's appointments
     */
    public List<StylistAppointmentDTO> getTodayAppointments(String stylistEmail) {
        User stylist = userRepository.findByEmail(stylistEmail)
                .orElseThrow(() -> new IllegalArgumentException("Stylist not found"));

        OffsetDateTime todayStart = LocalDate.now(SALON_TIMEZONE)
                .atStartOfDay(SALON_TIMEZONE).toOffsetDateTime();
        OffsetDateTime todayEnd = todayStart.plusDays(1);

        List<Appointment> appointments = appointmentRepository
                .findByStylistAndDateRange(stylist.getId(), todayStart, todayEnd);

        return appointments.stream()
                .map(this::mapToDTO)
                .sorted((a, b) -> a.getStartTs().compareTo(b.getStartTs()))
                .collect(Collectors.toList());
    }

    /**
     * Get pending confirmations
     */
    public List<StylistAppointmentDTO> getPendingConfirmations(String stylistEmail) {
        User stylist = userRepository.findByEmail(stylistEmail)
                .orElseThrow(() -> new IllegalArgumentException("Stylist not found"));

        OffsetDateTime now = OffsetDateTime.now(SALON_TIMEZONE);
        OffsetDateTime futureLimit = now.plusDays(30);

        List<Appointment> appointments = appointmentRepository
                .findByStylistAndDateRange(stylist.getId(), now, futureLimit);

        return appointments.stream()
                .filter(a -> "BOOKED".equals(a.getStatus()))
                .map(this::mapToDTO)
                .sorted((a, b) -> a.getStartTs().compareTo(b.getStartTs()))
                .collect(Collectors.toList());
    }

    /**
     * Confirm appointment
     */
    @Transactional
    public void confirmAppointment(Long appointmentId, String stylistEmail) {
        User stylist = userRepository.findByEmail(stylistEmail)
                .orElseThrow(() -> new IllegalArgumentException("Stylist not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        // Security check
        if (!appointment.getStylist().getId().equals(stylist.getId())) {
            throw new AccessDeniedException("Cannot confirm other stylist's appointments");
        }

        // Status validation
        if (!"BOOKED".equals(appointment.getStatus())) {
            throw new IllegalStateException("Can only confirm BOOKED appointments");
        }

        appointment.setStatus("CONFIRMED");
        appointmentRepository.save(appointment);
    }

    /**
     * Complete appointment
     */
    @Transactional
    public void completeAppointment(Long appointmentId, String stylistEmail) {
        User stylist = userRepository.findByEmail(stylistEmail)
                .orElseThrow(() -> new IllegalArgumentException("Stylist not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appointment.getStylist().getId().equals(stylist.getId())) {
            throw new AccessDeniedException("Cannot complete other stylist's appointments");
        }

        if (!"CONFIRMED".equals(appointment.getStatus())) {
            throw new IllegalStateException("Can only complete CONFIRMED appointments");
        }

        appointment.setStatus("COMPLETED");
        appointmentRepository.save(appointment);
    }

    /**
     * Mark no-show
     */
    @Transactional
    public void markNoShow(Long appointmentId, String stylistEmail) {
        User stylist = userRepository.findByEmail(stylistEmail)
                .orElseThrow(() -> new IllegalArgumentException("Stylist not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appointment.getStylist().getId().equals(stylist.getId())) {
            throw new AccessDeniedException("Cannot mark no-show for other stylist's appointments");
        }

        appointment.setStatus("NO_SHOW");
        appointmentRepository.save(appointment);
    }

    /**
     * Cancel appointment
     */
    @Transactional
    public void cancelAppointment(Long appointmentId, String stylistEmail) {
        User stylist = userRepository.findByEmail(stylistEmail)
                .orElseThrow(() -> new IllegalArgumentException("Stylist not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appointment.getStylist().getId().equals(stylist.getId())) {
            throw new AccessDeniedException("Cannot cancel other stylist's appointments");
        }

        appointment.setStatus("CANCELLED");
        appointmentRepository.save(appointment);
    }

    /**
     * Map entity to DTO
     */
    private StylistAppointmentDTO mapToDTO(Appointment appointment) {
        StylistAppointmentDTO dto = new StylistAppointmentDTO();
        dto.setId(appointment.getId());
        dto.setClientId(appointment.getClient().getId());
        dto.setClientName(appointment.getClient().getFullName());
        dto.setClientEmail(appointment.getClient().getEmail());
        dto.setClientPhone(appointment.getClient().getPhone());
        dto.setStartTs(appointment.getStartTs());
        dto.setEndTs(appointment.getEndTs());
        dto.setStatus(appointment.getStatus());
        dto.setTotalPrice(appointment.getTotalPrice());
        dto.setNotes(appointment.getNotes());
        dto.setCreatedAt(appointment.getCreatedAt());

        List<StylistAppointmentDTO.ServiceItemDTO> services = appointment.getItems().stream()
                .map(item -> {
                    StylistAppointmentDTO.ServiceItemDTO serviceDTO = new StylistAppointmentDTO.ServiceItemDTO();
                    serviceDTO.setServiceId(item.getService().getId());
                    serviceDTO.setServiceName(item.getService().getName());
                    serviceDTO.setDurationMin(item.getDurationMin());
                    serviceDTO.setPrice(item.getPrice());
                    return serviceDTO;
                })
                .collect(Collectors.toList());
        dto.setServices(services);

        return dto;
    }
}