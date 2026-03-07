package com.saloon.saloon_backend.service;

import com.saloon.saloon_backend.dto.CapacityAlertDTO;
import com.saloon.saloon_backend.entity.CapacityAlert;
import com.saloon.saloon_backend.entity.SlotConfiguration;
import com.saloon.saloon_backend.entity.User;
import com.saloon.saloon_backend.entity.enums.UserRole;
import com.saloon.saloon_backend.repository.AppointmentRepository;
import com.saloon.saloon_backend.repository.CapacityAlertRepository;
import com.saloon.saloon_backend.repository.SlotConfigurationRepository;
import com.saloon.saloon_backend.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CapacityMonitoringService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final CapacityAlertRepository capacityAlertRepository;
    private final SlotConfigurationRepository slotConfigRepository;

    public CapacityMonitoringService(
            AppointmentRepository appointmentRepository,
            UserRepository userRepository,
            CapacityAlertRepository capacityAlertRepository,
            SlotConfigurationRepository slotConfigRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.capacityAlertRepository = capacityAlertRepository;
        this.slotConfigRepository = slotConfigRepository;
    }

    /**
     * Run every 15 minutes
     */
    @Scheduled(fixedRate = 15 * 60 * 1000)
    @Transactional
    public void monitorCapacity() {

        // ✅ FIXED: no ZoneOffset.plusMinutes() (not supported)
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.ofHoursMinutes(5, 30));

        for (int i = 0; i < 6; i++) {
            OffsetDateTime checkTime = now.plusHours(i);

            checkCapacityForHour(
                    checkTime.toLocalDate(),
                    checkTime.getHour()
            );
        }
    }

    /**
     * Check capacity for specific date & hour
     */
    private void checkCapacityForHour(LocalDate date, int hour) {

        OffsetDateTime start = OffsetDateTime.of(
                date,
                LocalTime.of(hour, 0),
                ZoneOffset.ofHoursMinutes(5, 30)
        );

        OffsetDateTime end = start.plusHours(1);

        List<User> stylists = userRepository.findByRole(UserRole.STYLIST);

        for (User stylist : stylists) {

            // ✅ FIXED: uses repo method that exists (added earlier)
            long bookingCount = appointmentRepository.countBookingsForStylistInTimeRange(
                    stylist.getId(),
                    start,
                    end
            );

            SlotConfiguration config = getSlotConfiguration(
                    start.getDayOfWeek().getValue(),
                    hour
            );

            int maxCapacity = Boolean.TRUE.equals(config.getIsPeakHour())
                    ? config.getPeakCapacity()
                    : config.getBaseCapacity();

            if (maxCapacity <= 0) maxCapacity = 1;

            double usage = (double) bookingCount / maxCapacity * 100;

            if (usage >= 80) {
                createAlert(stylist, date, hour, usage);
            }
        }
    }

    /**
     * Create alert
     */
    private void createAlert(User stylist, LocalDate date, int hour, double usage) {

        CapacityAlert alert = new CapacityAlert();
        alert.setAlertDate(date);
        alert.setAlertTime(LocalTime.of(hour, 0));
        alert.setAlertType("CAPACITY_WARNING");

        if (usage >= 100) {
            alert.setSeverity("CRITICAL");
        } else if (usage >= 90) {
            alert.setSeverity("HIGH");
        } else {
            alert.setSeverity("MEDIUM");
        }

        alert.setStylist(stylist);
        alert.setMessage("High booking load detected for this time slot.");
        alert.setIsResolved(false);
        alert.setCreatedAt(OffsetDateTime.now(ZoneOffset.ofHoursMinutes(5, 30)));

        capacityAlertRepository.save(alert);
    }

    /**
     * Get slot configuration
     */
    private SlotConfiguration getSlotConfiguration(Integer dayOfWeek, Integer hourOfDay) {

        Optional<SlotConfiguration> optional =
                slotConfigRepository.findByDayOfWeekAndHourOfDay(dayOfWeek, hourOfDay);

        return optional.orElseGet(() ->
                createDefaultConfig(dayOfWeek, hourOfDay)
        );
    }

    /**
     * Map entity to DTO
     */
    private CapacityAlertDTO mapToDTO(CapacityAlert alert) {
        CapacityAlertDTO dto = new CapacityAlertDTO();
        dto.setId(alert.getId());
        dto.setAlertDate(alert.getAlertDate());
        dto.setAlertTime(alert.getAlertTime());
        dto.setAlertType(alert.getAlertType());
        dto.setSeverity(alert.getSeverity());
        dto.setStylistName(alert.getStylist() != null ? alert.getStylist().getFullName() : "N/A");
        dto.setMessage(alert.getMessage());
        dto.setIsResolved(alert.getIsResolved());
        dto.setCreatedAt(alert.getCreatedAt());
        dto.setResolvedAt(alert.getResolvedAt());
        return dto;
    }

    /**
     * Get all active alerts
     */
    public List<CapacityAlertDTO> getActiveAlerts() {
        return capacityAlertRepository
                // ✅ Now this compiles because we added it to the repository
                .findByIsResolvedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Resolve alert
     */
    @Transactional
    public void resolveAlert(Long id) {
        CapacityAlert alert = capacityAlertRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        alert.setIsResolved(true);
        alert.setResolvedAt(OffsetDateTime.now(ZoneOffset.ofHoursMinutes(5, 30)));

        capacityAlertRepository.save(alert);
    }

    /**
     * Create default slot configuration
     */
    private SlotConfiguration createDefaultConfig(Integer dayOfWeek, Integer hourOfDay) {
        SlotConfiguration config = new SlotConfiguration();
        config.setDayOfWeek(dayOfWeek);
        config.setHourOfDay(hourOfDay);
        config.setBaseCapacity(4);
        config.setPeakCapacity(6);
        config.setIsPeakHour(false);
        config.setSlotDurationMin(30);
        return slotConfigRepository.save(config);
    }

    /**
     * Initialize slot configurations (Run once)
     */
    @Transactional
    public void initializeSlotConfigurations() {

        for (int day = 1; day <= 7; day++) {
            for (int hour = 9; hour <= 18; hour++) {

                if (!slotConfigRepository.findByDayOfWeekAndHourOfDay(day, hour).isPresent()) {
                    SlotConfiguration config = new SlotConfiguration();
                    config.setDayOfWeek(day);
                    config.setHourOfDay(hour);
                    config.setBaseCapacity(4);
                    config.setPeakCapacity(6);
                    config.setIsPeakHour(false);
                    config.setSlotDurationMin(30);
                    slotConfigRepository.save(config);
                }
            }
        }

        System.out.println("✅ Slot configurations initialized");
    }
}