package com.saloon.saloon_backend.service;

import com.saloon.saloon_backend.dto.IdleCapacityAlertDTO;
import com.saloon.saloon_backend.entity.*;
import com.saloon.saloon_backend.entity.enums.UserRole;
import com.saloon.saloon_backend.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IdleCapacityService {

    private final IdleCapacityAlertRepository alertRepository;
    private final AppointmentRepository appointmentRepository;
    private final PeakHourPredictionRepository predictionRepository;
    private final UserRepository userRepository;

    private static final int EXPECTED_CAPACITY = 4; // 4 bookings per hour
    private static final BigDecimal AVERAGE_APPOINTMENT_VALUE = BigDecimal.valueOf(80);

    public IdleCapacityService(
            IdleCapacityAlertRepository alertRepository,
            AppointmentRepository appointmentRepository,
            PeakHourPredictionRepository predictionRepository,
            UserRepository userRepository
    ) {
        this.alertRepository = alertRepository;
        this.appointmentRepository = appointmentRepository;
        this.predictionRepository = predictionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Run daily at 8 PM to analyze today's capacity utilization
     */
    @Scheduled(cron = "0 0 20 * * *") // Every day at 8 PM
    @Transactional
    public void analyzeIdleCapacity() {
        LocalDate today = LocalDate.now();
        System.out.println("🔍 Analyzing idle capacity for: " + today);

        List<User> stylists = userRepository.findByRole(UserRole.valueOf("STYLIST"));

        for (User stylist : stylists) {
            analyzeStylistCapacity(stylist, today);
        }
    }

    /**
     * Analyze capacity for a specific stylist on a specific date
     */
    @Transactional
    public void analyzeStylistCapacity(User stylist, LocalDate date) {
        for (int hour = 9; hour <= 18; hour++) {
            int actualBookings = countBookings(stylist.getId(), date, hour);
            int expectedBookings = getExpectedBookings(date.getDayOfWeek(), hour);

            if (actualBookings < expectedBookings) {
                int missedBookings = expectedBookings - actualBookings;
                BigDecimal idlePercentage = BigDecimal.valueOf(missedBookings)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(expectedBookings), 2, RoundingMode.HALF_UP);

                BigDecimal revenueLoss = AVERAGE_APPOINTMENT_VALUE
                        .multiply(BigDecimal.valueOf(missedBookings));

                String alertLevel = determineAlertLevel(idlePercentage);

                // Only create alert if idle percentage > 30%
                if (idlePercentage.compareTo(BigDecimal.valueOf(30)) > 0) {
                    IdleCapacityAlert alert = new IdleCapacityAlert();
                    alert.setAlertDate(date);
                    alert.setAlertHour(hour);
                    alert.setStylistId(stylist.getId());
                    alert.setExpectedBookings(expectedBookings);
                    alert.setActualBookings(actualBookings);
                    alert.setIdlePercentage(idlePercentage);
                    alert.setRevenueLossEstimate(revenueLoss);
                    alert.setAlertLevel(alertLevel);
                    alert.setIsResolved(false);

                    alertRepository.save(alert);

                    System.out.println("⚠️ Idle capacity alert: " + stylist.getFullName() +
                            " | " + date + " " + hour + ":00 | " + idlePercentage + "% idle");
                }
            }
        }
    }

    /**
     * Count actual bookings for a specific hour
     */
    private int countBookings(Long stylistId, LocalDate date, int hour) {
        OffsetDateTime hourStart = OffsetDateTime.of(
                date, LocalTime.of(hour, 0),
                ZoneOffset.ofHoursMinutes(5, 30)
        );
        OffsetDateTime hourEnd = hourStart.plusHours(1);

        return (int) appointmentRepository.findAll().stream()
                .filter(apt -> apt.getStylist().getId().equals(stylistId))
                .filter(apt -> !"CANCELLED".equals(apt.getStatus()))
                .filter(apt -> apt.getStartTs().isAfter(hourStart) && apt.getStartTs().isBefore(hourEnd))
                .count();
    }

    /**
     * Get expected bookings from predictions
     */
    private int getExpectedBookings(DayOfWeek dayOfWeek, int hour) {
        Optional<PeakHourPrediction> prediction = predictionRepository
                .findByDayOfWeekAndHourOfDay(dayOfWeek.getValue(), hour);

        return prediction.map(PeakHourPrediction::getPredictedBookings)
                .orElse(EXPECTED_CAPACITY);
    }

    /**
     * Determine alert severity level
     */
    private String determineAlertLevel(BigDecimal idlePercentage) {
        if (idlePercentage.compareTo(BigDecimal.valueOf(75)) >= 0) {
            return "CRITICAL";
        } else if (idlePercentage.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "HIGH";
        } else if (idlePercentage.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Get unresolved idle capacity alerts
     */
    public List<IdleCapacityAlertDTO> getUnresolvedAlerts() {
        List<IdleCapacityAlert> alerts = alertRepository.findByIsResolvedFalseOrderByCreatedAtDesc();
        return alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get alerts for a specific date range
     */
    public List<IdleCapacityAlertDTO> getAlertsForDateRange(LocalDate startDate, LocalDate endDate) {
        List<IdleCapacityAlert> alerts = alertRepository
                .findByAlertDateBetweenAndIsResolvedFalse(startDate, endDate);
        return alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Mark alert as resolved
     */
    @Transactional
    public void resolveAlert(Long alertId) {
        Optional<IdleCapacityAlert> alert = alertRepository.findById(alertId);
        if (alert.isPresent()) {
            IdleCapacityAlert a = alert.get();
            a.setIsResolved(true);
            a.setResolvedAt(OffsetDateTime.now());
            alertRepository.save(a);
        }
    }

    /**
     * Convert entity to DTO
     */
    private IdleCapacityAlertDTO convertToDTO(IdleCapacityAlert alert) {
        IdleCapacityAlertDTO dto = new IdleCapacityAlertDTO();
        dto.setId(alert.getId());
        dto.setAlertDate(alert.getAlertDate());
        dto.setAlertHour(alert.getAlertHour());
        dto.setTimeSlot(formatTimeSlot(alert.getAlertHour()));
        dto.setStylistId(alert.getStylistId());

        if (alert.getStylistId() != null) {
            userRepository.findById(alert.getStylistId()).ifPresent(stylist -> {
                dto.setStylistName(stylist.getFullName());
            });
        }

        dto.setExpectedBookings(alert.getExpectedBookings());
        dto.setActualBookings(alert.getActualBookings());
        dto.setIdlePercentage(alert.getIdlePercentage());
        dto.setRevenueLossEstimate(alert.getRevenueLossEstimate());
        dto.setAlertLevel(alert.getAlertLevel());
        dto.setIsResolved(alert.getIsResolved());

        return dto;
    }

    /**
     * Format hour as time slot string
     */
    private String formatTimeSlot(int hour) {
        LocalTime start = LocalTime.of(hour, 0);
        LocalTime end = start.plusHours(1);
        return start.toString() + " - " + end.toString();
    }
}