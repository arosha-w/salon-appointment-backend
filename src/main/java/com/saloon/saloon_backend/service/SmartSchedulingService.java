// ============================================================================
// FILE: service/SmartSchedulingService.java
// PURPOSE: AI-powered slot recommendations for smart rescheduling
// FEATURES:
// - Analyzes multiple factors (demand, time similarity, stylist rating)
// - Scores each slot (0-100)
// - Returns top 10 recommendations
// - Considers client's previous booking patterns
// ============================================================================
package com.saloon.saloon_backend.service;

import com.saloon.saloon_backend.dto.SmartSlotRecommendationDTO;
import com.saloon.saloon_backend.entity.*;
import com.saloon.saloon_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SmartSchedulingService {

    private final AppointmentRepository appointmentRepository;
    private final PeakHourPredictionRepository predictionRepository;
    private final StylistProfileRepository stylistProfileRepository;
    private final UserRepository userRepository;
    private final SlotLockService slotLockService;

    public SmartSchedulingService(
            AppointmentRepository appointmentRepository,
            PeakHourPredictionRepository predictionRepository,
            StylistProfileRepository stylistProfileRepository,
            UserRepository userRepository,
            SlotLockService slotLockService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.predictionRepository = predictionRepository;
        this.stylistProfileRepository = stylistProfileRepository;
        this.userRepository = userRepository;
        this.slotLockService = slotLockService;
    }

    /**
     * Get smart slot recommendations for rescheduling
     * Returns top 10 best slots based on multiple factors
     */
    @Transactional
    public List<SmartSlotRecommendationDTO> getSmartRecommendations(
            Long originalAppointmentId,
            LocalDate preferredDate
    ) {
        // Get original appointment
        Appointment original = appointmentRepository.findById(originalAppointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        List<SmartSlotRecommendationDTO> recommendations = new ArrayList<>();
        LocalDate startDate = preferredDate != null ? preferredDate : LocalDate.now().plusDays(1);

        // Analyze next 7 days
        for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
            LocalDate checkDate = startDate.plusDays(dayOffset);
            DayOfWeek dayOfWeek = checkDate.getDayOfWeek();

            // Check each hour from 9 AM to 6 PM
            for (int hour = 9; hour <= 18; hour++) {
                LocalTime time = LocalTime.of(hour, 0);

                SmartSlotRecommendationDTO recommendation = evaluateSlot(
                        original,
                        checkDate,
                        time,
                        dayOfWeek
                );

                if (recommendation != null && recommendation.getScore().compareTo(BigDecimal.valueOf(25)) > 0) {
                    recommendations.add(recommendation);
                }
            }
        }

        // Sort by score (highest first), then by date (earliest first)
        recommendations.sort((a, b) -> {
            int scoreCompare = b.getScore().compareTo(a.getScore());
            if (scoreCompare != 0) return scoreCompare;

            int dateCompare = a.getRecommendedDate().compareTo(b.getRecommendedDate());
            if (dateCompare != 0) return dateCompare;

            return a.getRecommendedTime().compareTo(b.getRecommendedTime());
        });

        // Return top 10
        return recommendations.stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Evaluate a specific slot and calculate its recommendation score
     */
    @Transactional
    private SmartSlotRecommendationDTO evaluateSlot(
            Appointment original,
            LocalDate date,
            LocalTime time,
            DayOfWeek dayOfWeek
    ) {
        Long stylistId = original.getStylist().getId();
        int durationMinutes = (int) Duration.between(
                original.getStartTs(),
                original.getEndTs()
        ).toMinutes();

        // Check if slot is available
        if (!isSlotAvailable(stylistId, date, time, durationMinutes)) {
            return null;
        }

        // Calculate score based on multiple factors
        BigDecimal score = BigDecimal.ZERO;
        Map<String, Object> factors = new HashMap<>();

        // FACTOR 1: Demand Level (40 points max)
        int hour = time.getHour();
        int dayNum = dayOfWeek.getValue();

        Optional<PeakHourPrediction> prediction = predictionRepository
                .findByDayOfWeekAndHourOfDay(dayNum, hour);

        String demandLevel = "MEDIUM";
        if (prediction.isPresent()) {
            int predictedBookings = prediction.get().getPredictedBookings();

            if (predictedBookings < 3) {
                score = score.add(BigDecimal.valueOf(40));
                demandLevel = "LOW";
                factors.put("demand", "LOW");
            } else if (predictedBookings < 5) {
                score = score.add(BigDecimal.valueOf(25));
                demandLevel = "MEDIUM";
                factors.put("demand", "MEDIUM");
            } else {
                score = score.add(BigDecimal.valueOf(10));
                demandLevel = "HIGH";
                factors.put("demand", "HIGH");
            }
        } else {
            // No prediction data - assume medium
            score = score.add(BigDecimal.valueOf(20));
        }

        // FACTOR 2: Same Day of Week (20 points)
        DayOfWeek originalDay = original.getStartTs().getDayOfWeek();
        if (dayOfWeek == originalDay) {
            score = score.add(BigDecimal.valueOf(20));
            factors.put("sameDayOfWeek", true);
        }

        // FACTOR 3: Similar Time (20 points)
        int originalHour = original.getStartTs().getHour();
        int timeDiff = Math.abs(hour - originalHour);

        if (timeDiff == 0) {
            score = score.add(BigDecimal.valueOf(20));
            factors.put("exactSameTime", true);
        } else if (timeDiff == 1) {
            score = score.add(BigDecimal.valueOf(15));
            factors.put("similarTime", true);
        } else if (timeDiff == 2) {
            score = score.add(BigDecimal.valueOf(10));
        }

        // FACTOR 4: Urgency - Earlier is Better (10 points)
        long daysUntil = Duration.between(
                LocalDate.now().atStartOfDay(),
                date.atStartOfDay()
        ).toDays();

        if (daysUntil <= 1) {
            score = score.add(BigDecimal.valueOf(10));
            factors.put("urgency", "TOMORROW");
        } else if (daysUntil <= 3) {
            score = score.add(BigDecimal.valueOf(7));
            factors.put("urgency", "SOON");
        }

        // FACTOR 5: Stylist Rating (10 points)
        Optional<StylistProfile> profile = stylistProfileRepository.findByUserId(stylistId);
        if (profile.isPresent()) {
            BigDecimal rating = profile.get().getRating();
            if (rating != null && rating.compareTo(BigDecimal.valueOf(4.5)) >= 0) {
                score = score.add(BigDecimal.valueOf(10));
                factors.put("highRatedStylist", true);
            } else if (rating != null && rating.compareTo(BigDecimal.valueOf(4.0)) >= 0) {
                score = score.add(BigDecimal.valueOf(5));
            }
        }

        // Generate human-readable reason
        String reason = generateReasonText(factors, date, time);

        // Get capacity info
        int currentBookings = countBookingsForSlot(stylistId, date, hour);
        int availableCapacity = 4 - currentBookings; // Assume capacity of 4

        // Format date/time for display
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        String formattedDateTime = date.format(dateFormatter) + " at " + time.format(timeFormatter);

        // Build DTO
        SmartSlotRecommendationDTO dto = new SmartSlotRecommendationDTO();
        dto.setStylistId(stylistId);
        dto.setStylistName(original.getStylist().getFullName());
        dto.setRecommendedDate(date);
        dto.setRecommendedTime(time);
        dto.setFormattedDateTime(formattedDateTime);
        dto.setScore(score);
        dto.setReason(reason);
        dto.setDemandLevel(demandLevel);
        dto.setAvailableCapacity(availableCapacity);
        dto.setFactors(factors);

        return dto;
    }

    /**
     * Check if slot is available (not booked and not locked)
     */
    @Transactional
    private boolean isSlotAvailable(
            Long stylistId,
            LocalDate date,
            LocalTime time,
            int durationMinutes
    ) {
        // Check if slot is locked
        if (slotLockService.isSlotLocked(stylistId, date, time)) {
            return false;
        }

        OffsetDateTime slotStart = OffsetDateTime.of(
                date, time,
                ZoneOffset.ofHoursMinutes(5, 30)
        );
        OffsetDateTime slotEnd = slotStart.plusMinutes(durationMinutes);

        // Add 5-minute buffer after appointments
        OffsetDateTime bufferStart = slotStart.minusMinutes(5);
        OffsetDateTime bufferEnd = slotEnd.plusMinutes(5);

        // Check for overlapping appointments
        long overlapping = appointmentRepository.findAll().stream()
                .filter(apt -> apt.getStylist().getId().equals(stylistId))
                .filter(apt -> !"CANCELLED".equals(apt.getStatus()))
                .filter(apt -> {
                    boolean startsBeforeEnd = apt.getStartTs().isBefore(bufferEnd);
                    boolean endsAfterStart = apt.getEndTs().isAfter(bufferStart);
                    return startsBeforeEnd && endsAfterStart;
                })
                .count();

        return overlapping == 0;
    }

    /**
     * Count existing bookings for a specific hour
     */
    @Transactional
    private int countBookingsForSlot(Long stylistId, LocalDate date, int hour) {
        OffsetDateTime hourStart = OffsetDateTime.of(
                date, LocalTime.of(hour, 0),
                ZoneOffset.ofHoursMinutes(5, 30)
        );
        OffsetDateTime hourEnd = hourStart.plusHours(1);

        return (int) appointmentRepository.findAll().stream()
                .filter(apt -> apt.getStylist().getId().equals(stylistId))
                .filter(apt -> !"CANCELLED".equals(apt.getStatus()))
                .filter(apt -> {
                    boolean startsInHour = apt.getStartTs().isAfter(hourStart) &&
                            apt.getStartTs().isBefore(hourEnd);
                    return startsInHour;
                })
                .count();
    }

    /**
     * Generate human-readable recommendation reason
     */
    @Transactional
    private String generateReasonText(
            Map<String, Object> factors,
            LocalDate date,
            LocalTime time
    ) {
        StringBuilder reason = new StringBuilder();

        // Demand
        String demand = (String) factors.get("demand");
        if ("LOW".equals(demand)) {
            reason.append("⭐ Quiet time - minimal wait expected. ");
        } else if ("MEDIUM".equals(demand)) {
            reason.append("✅ Good availability. ");
        } else if ("HIGH".equals(demand)) {
            reason.append("Moderate demand period. ");
        }

        // Same day
        if (factors.containsKey("sameDayOfWeek") && (Boolean) factors.get("sameDayOfWeek")) {
            reason.append("Same day of week as your original booking. ");
        }

        // Time similarity
        if (factors.containsKey("exactSameTime") && (Boolean) factors.get("exactSameTime")) {
            reason.append("🎯 Exact same time! ");
        } else if (factors.containsKey("similarTime") && (Boolean) factors.get("similarTime")) {
            reason.append("Similar time to your preference. ");
        }

        // Urgency
        String urgency = (String) factors.get("urgency");
        if ("TOMORROW".equals(urgency)) {
            reason.append("📅 Available tomorrow! ");
        } else if ("SOON".equals(urgency)) {
            reason.append("Available soon. ");
        }

        // Stylist
        if (factors.containsKey("highRatedStylist") && (Boolean) factors.get("highRatedStylist")) {
            reason.append("⭐ Top-rated stylist. ");
        }

        if (reason.length() == 0) {
            reason.append("Available slot.");
        }

        return reason.toString().trim();
    }

    /**
     * Get recommendations for a specific stylist and date range
     */
    @Transactional
    public List<SmartSlotRecommendationDTO> getRecommendationsForStylist(
            Long stylistId,
            LocalDate startDate,
            LocalDate endDate,
            int durationMinutes
    ) {
        User stylist = userRepository.findById(stylistId)
                .orElseThrow(() -> new IllegalArgumentException("Stylist not found"));

        List<SmartSlotRecommendationDTO> recommendations = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            for (int hour = 9; hour <= 18; hour++) {
                LocalTime time = LocalTime.of(hour, 0);

                if (isSlotAvailable(stylistId, currentDate, time, durationMinutes)) {
                    // Create simple recommendation
                    SmartSlotRecommendationDTO dto = new SmartSlotRecommendationDTO();
                    dto.setStylistId(stylistId);
                    dto.setStylistName(stylist.getFullName());
                    dto.setRecommendedDate(currentDate);
                    dto.setRecommendedTime(time);
                    dto.setFormattedDateTime(currentDate + " " + time);
                    dto.setScore(BigDecimal.valueOf(50)); // Default score
                    dto.setReason("Available slot");

                    recommendations.add(dto);
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        return recommendations;
    }
}