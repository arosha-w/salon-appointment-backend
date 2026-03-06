// service/ClientBookingService.java
package com.saloon.saloon_backend.service;

import com.saloon.saloon_backend.dto.BestTimeToBookDTO;
import com.saloon.saloon_backend.dto.TimeSlotAvailabilityDTO;
import com.saloon.saloon_backend.entity.PeakHourPrediction;
import com.saloon.saloon_backend.entity.SlotConfiguration;
import com.saloon.saloon_backend.entity.User;
import com.saloon.saloon_backend.repository.AppointmentRepository;
import com.saloon.saloon_backend.repository.PeakHourPredictionRepository;
import com.saloon.saloon_backend.repository.SlotConfigurationRepository;
import com.saloon.saloon_backend.repository.UserRepository;

// ✅ FIX: Add correct import for UserRole
import com.saloon.saloon_backend.entity.enums.UserRole;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClientBookingService {

    private final AppointmentRepository appointmentRepository;
    private final PeakHourPredictionRepository predictionRepository;
    private final SlotConfigurationRepository slotConfigRepository;
    private final UserRepository userRepository;

    public ClientBookingService(
            AppointmentRepository appointmentRepository,
            PeakHourPredictionRepository predictionRepository,
            SlotConfigurationRepository slotConfigRepository,
            UserRepository userRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.predictionRepository = predictionRepository;
        this.slotConfigRepository = slotConfigRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get available time slots with demand indicators for clients
     */
    public List<TimeSlotAvailabilityDTO> getAvailableSlotsWithDemand(
            Long stylistId,
            String dateStr,
            Integer durationMin
    ) {
        LocalDate date = LocalDate.parse(dateStr);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // Get all stylists if no specific stylist selected
        List<User> stylists;
        if (stylistId != null) {
            stylists = List.of(userRepository.findById(stylistId)
                    .orElseThrow(() -> new IllegalArgumentException("Stylist not found")));
        } else {
            // ✅ Now UserRole resolves correctly
            stylists = userRepository.findByRole(UserRole.STYLIST);
        }

        List<TimeSlotAvailabilityDTO> slots = new ArrayList<>();

        // Check each hour from 9 AM to 6 PM
        for (int hour = 9; hour <= 18; hour++) {
            LocalTime slotTime = LocalTime.of(hour, 0);

            OffsetDateTime slotStart = OffsetDateTime.of(
                    date, slotTime, ZoneOffset.ofHoursMinutes(5, 30)
            );
            OffsetDateTime slotEnd = slotStart.plusHours(1);

            int currentBookings = 0;
            for (User stylist : stylists) {
                currentBookings += countBookingsInRange(stylist.getId(), slotStart, slotEnd);
            }

            // Get prediction for this day/hour
            PeakHourPrediction prediction = predictionRepository
                    .findByDayOfWeekAndHourOfDay(dayOfWeek.getValue(), hour)
                    .orElse(null);

            // Get slot configuration
            SlotConfiguration config = slotConfigRepository
                    .findByDayOfWeekAndHourOfDay(dayOfWeek.getValue(), hour)
                    .orElse(createDefaultConfig(dayOfWeek.getValue(), hour));

            int totalCapacity = Boolean.TRUE.equals(config.getIsPeakHour())
                    ? config.getPeakCapacity() * stylists.size()
                    : config.getBaseCapacity() * stylists.size();

            boolean isAvailable = currentBookings < totalCapacity;

            double utilizationRate = totalCapacity > 0
                    ? (double) currentBookings / totalCapacity
                    : 0;

            String demandLevel = calculateDemandLevel(utilizationRate);
            String recommendation = generateRecommendation(utilizationRate, prediction);

            TimeSlotAvailabilityDTO slot = new TimeSlotAvailabilityDTO();
            slot.setTime(slotTime.toString());
            slot.setIsAvailable(isAvailable);
            slot.setDemandLevel(demandLevel);
            slot.setCurrentBookings(currentBookings);
            slot.setTotalCapacity(totalCapacity);
            slot.setPredictedBookings(prediction != null ? prediction.getPredictedBookings() : 0);
            slot.setRecommendation(recommendation);

            slots.add(slot);
        }

        return slots;
    }

    /**
     * Get best times to book for next 7 days
     */
    public List<BestTimeToBookDTO> getBestTimesToBook() {
        List<BestTimeToBookDTO> bestTimes = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
            LocalDate date = today.plusDays(dayOffset);
            DayOfWeek dayOfWeek = date.getDayOfWeek();

            int bestHour = -1;
            int lowestBookings = Integer.MAX_VALUE;

            for (int hour = 9; hour <= 18; hour++) {
                OffsetDateTime slotStart = OffsetDateTime.of(
                        date, LocalTime.of(hour, 0),
                        ZoneOffset.ofHoursMinutes(5, 30)
                );
                OffsetDateTime slotEnd = slotStart.plusHours(1);

                List<User> stylists = userRepository.findByRole(UserRole.STYLIST);
                int totalBookings = 0;
                for (User stylist : stylists) {
                    totalBookings += countBookingsInRange(stylist.getId(), slotStart, slotEnd);
                }

                if (totalBookings < lowestBookings) {
                    lowestBookings = totalBookings;
                    bestHour = hour;
                }
            }

            if (bestHour != -1) {
                LocalTime bestTime = LocalTime.of(bestHour, 0);
                List<User> stylists = userRepository.findByRole(UserRole.STYLIST);

                SlotConfiguration config = slotConfigRepository
                        .findByDayOfWeekAndHourOfDay(dayOfWeek.getValue(), bestHour)
                        .orElse(createDefaultConfig(dayOfWeek.getValue(), bestHour));

                int totalCapacity = config.getBaseCapacity() * stylists.size();
                double utilizationRate = totalCapacity > 0
                        ? (double) lowestBookings / totalCapacity
                        : 0;

                String reason = generateBestTimeReason(utilizationRate, lowestBookings);
                String demandLevel = calculateDemandLevel(utilizationRate);

                BestTimeToBookDTO bestTimeDTO = new BestTimeToBookDTO(
                        date,
                        bestTime,
                        dayOfWeek.toString(),
                        reason,
                        stylists.size(),
                        demandLevel
                );

                bestTimes.add(bestTimeDTO);
            }
        }

        return bestTimes;
    }

    /**
     * Get peak hours warning for clients
     */
    public Map<String, Object> getPeakHoursInfo() {
        Map<String, Object> info = new HashMap<>();

        List<PeakHourPrediction> topPeaks = predictionRepository
                .findTop10ByOrderByPredictedBookingsDesc()
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        List<Map<String, Object>> peakList = topPeaks.stream()
                .map(peak -> {
                    Map<String, Object> peakInfo = new HashMap<>();
                    peakInfo.put("dayOfWeek", getDayName(peak.getDayOfWeek()));
                    peakInfo.put("time", String.format("%02d:00", peak.getHourOfDay()));
                    peakInfo.put("expectedWait", calculateExpectedWait(peak.getPredictedBookings()));
                    peakInfo.put("demandLevel", peak.getPredictedBookings() >= 8 ? "VERY_HIGH" : "HIGH");
                    return peakInfo;
                })
                .collect(Collectors.toList());

        info.put("peakHours", peakList);
        info.put("recommendation", "Book outside these times for faster service");

        return info;
    }

    // Helper methods
    private int countBookingsInRange(Long stylistId, OffsetDateTime start, OffsetDateTime end) {
        return (int) appointmentRepository.findAll().stream()
                .filter(apt -> apt.getStylist() != null && apt.getStylist().getId().equals(stylistId))
                .filter(apt -> apt.getStartTs() != null)
                .filter(apt -> apt.getStartTs().isAfter(start) || apt.getStartTs().isEqual(start))
                .filter(apt -> apt.getStartTs().isBefore(end))
                .filter(apt -> "BOOKED".equals(apt.getStatus()) || "CONFIRMED".equals(apt.getStatus()))
                .count();
    }

    private String calculateDemandLevel(double utilizationRate) {
        if (utilizationRate >= 0.9) return "CRITICAL";
        if (utilizationRate >= 0.7) return "HIGH";
        if (utilizationRate >= 0.4) return "MEDIUM";
        return "LOW";
    }

    private String generateRecommendation(double utilizationRate, PeakHourPrediction prediction) {
        if (utilizationRate >= 0.9) {
            return "⚠️ Very busy - expect wait times";
        } else if (utilizationRate >= 0.7) {
            return "🔥 Popular time - book early";
        } else if (utilizationRate >= 0.4) {
            return "✅ Good time to book";
        } else {
            return "⭐ Best time - quiet period";
        }
    }

    private String generateBestTimeReason(double utilizationRate, int bookings) {
        if (utilizationRate < 0.3) {
            return "Quietest time of the day - minimal wait";
        } else if (utilizationRate < 0.5) {
            return "Low demand - fast service";
        } else {
            return "Moderate demand - good availability";
        }
    }

    private String calculateExpectedWait(int predictedBookings) {
        if (predictedBookings >= 8) return "15-30 minutes";
        if (predictedBookings >= 6) return "10-15 minutes";
        if (predictedBookings >= 4) return "5-10 minutes";
        return "Minimal wait";
    }

    private String getDayName(int dayOfWeek) {
        String[] days = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        return dayOfWeek >= 1 && dayOfWeek <= 7 ? days[dayOfWeek] : "Unknown";
    }

    private SlotConfiguration createDefaultConfig(Integer dayOfWeek, Integer hourOfDay) {
        SlotConfiguration config = new SlotConfiguration();
        config.setDayOfWeek(dayOfWeek);
        config.setHourOfDay(hourOfDay);
        config.setBaseCapacity(4);
        config.setPeakCapacity(6);
        config.setIsPeakHour(false);
        config.setSlotDurationMin(30);
        return config;
    }
}