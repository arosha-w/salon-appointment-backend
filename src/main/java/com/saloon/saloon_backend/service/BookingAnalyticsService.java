// service/BookingAnalyticsService.java
package com.saloon.saloon_backend.service;

import com.saloon.saloon_backend.dto.BookingTrendDTO;
import com.saloon.saloon_backend.dto.PeakHourDTO;
import com.saloon.saloon_backend.entity.*;
import com.saloon.saloon_backend.entity.enums.UserRole;
import com.saloon.saloon_backend.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingAnalyticsService {

    private final AppointmentRepository appointmentRepository;
    private final BookingAnalyticsRepository analyticsRepository;
    private final PeakHourPredictionRepository predictionRepository;
    private final DailyStatsRepository dailyStatsRepository;
    private final UserRepository userRepository;

    public BookingAnalyticsService(
            AppointmentRepository appointmentRepository,
            BookingAnalyticsRepository analyticsRepository,
            PeakHourPredictionRepository predictionRepository,
            DailyStatsRepository dailyStatsRepository,
            UserRepository userRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.analyticsRepository = analyticsRepository;
        this.predictionRepository = predictionRepository;
        this.dailyStatsRepository = dailyStatsRepository;
        this.userRepository = userRepository;
    }

    /**
     * Calculate daily analytics - Runs every day at 1 AM
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void calculateDailyAnalytics() {
        System.out.println("Starting daily analytics calculation...");

        LocalDate yesterday = LocalDate.now().minusDays(1);

        // Get all completed appointments from yesterday
        OffsetDateTime dayStart = yesterday.atStartOfDay(ZoneId.of("Asia/Colombo")).toOffsetDateTime();
        OffsetDateTime dayEnd = dayStart.plusDays(1);

        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(apt -> apt.getStartTs().isAfter(dayStart) &&
                        apt.getStartTs().isBefore(dayEnd) &&
                        "COMPLETED".equals(apt.getStatus()))
                .collect(Collectors.toList());

        if (appointments.isEmpty()) {
            System.out.println("No completed appointments found for " + yesterday);
            return;
        }

        // Get all stylists
        List<User> stylists = userRepository.findByRole(UserRole.STYLIST);

        // Calculate analytics for each stylist and hour
        for (User stylist : stylists) {
            List<Appointment> stylistAppointments = appointments.stream()
                    .filter(apt -> apt.getStylist().getId().equals(stylist.getId()))
                    .collect(Collectors.toList());

            if (stylistAppointments.isEmpty()) continue;

            // Group by hour
            Map<Integer, List<Appointment>> byHour = stylistAppointments.stream()
                    .collect(Collectors.groupingBy(apt -> apt.getStartTs().getHour()));

            // Calculate stats for each hour
            byHour.forEach((hour, aptList) -> {
                BookingAnalytics analytics = analyticsRepository
                        .findByDateAndHourOfDayAndStylistId(yesterday, hour, stylist.getId())
                        .orElse(new BookingAnalytics());

                analytics.setDate(yesterday);
                analytics.setHourOfDay(hour);
                analytics.setDayOfWeek(yesterday.getDayOfWeek().getValue());
                analytics.setStylist(stylist);
                analytics.setTotalBookings(aptList.size());

                BigDecimal revenue = aptList.stream()
                        .map(Appointment::getTotalPrice)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                analytics.setTotalRevenue(revenue);

                double avgDuration = aptList.stream()
                        .mapToLong(apt -> ChronoUnit.MINUTES.between(apt.getStartTs(), apt.getEndTs()))
                        .average()
                        .orElse(0);
                analytics.setAvgDurationMin((int) Math.round(avgDuration));

                analyticsRepository.save(analytics);
            });
        }

        // Calculate daily stats
        calculateDailyStats(yesterday, appointments);

        System.out.println("Daily analytics calculation completed for " + yesterday);
    }

    /**
     * Calculate overall daily statistics
     */
    @Transactional
    private void calculateDailyStats(LocalDate date, List<Appointment> appointments) {
        DailyStats stats = dailyStatsRepository.findByStatDate(date)
                .orElse(new DailyStats());

        stats.setStatDate(date);
        stats.setTotalAppointments(appointments.size());
        stats.setCompletedAppointments((int) appointments.stream()
                .filter(a -> "COMPLETED".equals(a.getStatus())).count());
        stats.setCancelledAppointments((int) appointments.stream()
                .filter(a -> "CANCELLED".equals(a.getStatus())).count());

        BigDecimal totalRevenue = appointments.stream()
                .filter(a -> "COMPLETED".equals(a.getStatus()))
                .map(Appointment::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalRevenue(totalRevenue);

        if (stats.getCompletedAppointments() > 0) {
            BigDecimal avgValue = totalRevenue.divide(
                    BigDecimal.valueOf(stats.getCompletedAppointments()),
                    2,
                    RoundingMode.HALF_UP
            );
            stats.setAvgAppointmentValue(avgValue);
        }

        // Find peak hour
        Map<Integer, Long> hourCounts = appointments.stream()
                .collect(Collectors.groupingBy(
                        apt -> apt.getStartTs().getHour(),
                        Collectors.counting()
                ));

        Optional<Map.Entry<Integer, Long>> peakHourEntry = hourCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue());
        peakHourEntry.ifPresent(entry -> stats.setPeakHour(entry.getKey()));

        stats.setUpdatedAt(OffsetDateTime.now());
        dailyStatsRepository.save(stats);
    }

    /**
     * Predict peak hours - Runs every Sunday at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    @Transactional
    public void predictPeakHours() {
        System.out.println("Starting peak hour prediction...");

        // Get last 30 days of analytics
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<BookingAnalytics> historicalData = analyticsRepository.findByDateAfter(thirtyDaysAgo);

        if (historicalData.isEmpty()) {
            System.out.println("Not enough historical data for predictions");
            return;
        }

        // Group by day of week and hour
        Map<String, List<BookingAnalytics>> grouped = historicalData.stream()
                .collect(Collectors.groupingBy(a -> a.getDayOfWeek() + "-" + a.getHourOfDay()));

        // Calculate predictions for each day-hour combination
        grouped.forEach((key, dataList) -> {
            String[] parts = key.split("-");
            int dayOfWeek = Integer.parseInt(parts[0]);
            int hourOfDay = Integer.parseInt(parts[1]);

            // Calculate average bookings
            double avgBookings = dataList.stream()
                    .mapToInt(BookingAnalytics::getTotalBookings)
                    .average()
                    .orElse(0);

            // Calculate standard deviation for confidence
            double stdDev = calculateStandardDeviation(
                    dataList.stream()
                            .map(BookingAnalytics::getTotalBookings)
                            .map(Integer::doubleValue)
                            .collect(Collectors.toList())
            );

            // Confidence score: higher when low standard deviation
            double confidence = avgBookings > 0
                    ? Math.max(0, Math.min(1, 1 - (stdDev / avgBookings)))
                    : 0;

            // Save or update prediction
            PeakHourPrediction prediction = predictionRepository
                    .findByDayOfWeekAndHourOfDay(dayOfWeek, hourOfDay)
                    .orElse(new PeakHourPrediction());

            prediction.setDayOfWeek(dayOfWeek);
            prediction.setHourOfDay(hourOfDay);
            prediction.setPredictedBookings((int) Math.ceil(avgBookings));
            prediction.setConfidenceScore(
                    BigDecimal.valueOf(confidence).setScale(2, RoundingMode.HALF_UP)
            );
            prediction.setPredictionType("HISTORICAL_AVERAGE");
            prediction.setLastCalculated(OffsetDateTime.now());

            predictionRepository.save(prediction);
        });

        System.out.println("Peak hour prediction completed. Predictions saved: " + grouped.size());
    }

    /**
     * Calculate standard deviation
     */
    @Transactional
    private double calculateStandardDeviation(List<Double> values) {
        if (values.isEmpty()) return 0;

        double mean = values.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        double variance = values.stream()
                .mapToDouble(val -> Math.pow(val - mean, 2))
                .average()
                .orElse(0);

        return Math.sqrt(variance);
    }

    /**
     * Get peak hours for a specific day
     */
    @Transactional
    public List<PeakHourDTO> getPeakHoursForDay(DayOfWeek day) {
        List<PeakHourPrediction> predictions = predictionRepository
                .findByDayOfWeek(day.getValue());

        return predictions.stream()
                .sorted((a, b) -> b.getPredictedBookings().compareTo(a.getPredictedBookings()))
                .limit(10) // Top 10 peak hours
                .map(this::mapToPeakHourDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get peak hours for entire week
     */
    @Transactional
    public Map<String, List<PeakHourDTO>> getWeeklyPeakHours() {
        Map<String, List<PeakHourDTO>> weeklyPeaks = new LinkedHashMap<>();

        for (DayOfWeek day : DayOfWeek.values()) {
            List<PeakHourDTO> dayPeaks = getPeakHoursForDay(day);
            weeklyPeaks.put(day.name(), dayPeaks);
        }

        return weeklyPeaks;
    }

    /**
     * Get booking trends for last N days
     */
    @Transactional
    public List<BookingTrendDTO> getBookingTrends(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        List<DailyStats> stats = dailyStatsRepository.findByStatDateBetween(
                startDate,
                LocalDate.now()
        );

        return stats.stream()
                .map(stat -> {
                    BookingTrendDTO dto = new BookingTrendDTO(
                            stat.getStatDate(),
                            stat.getTotalAppointments(),
                            stat.getTotalRevenue()
                    );
                    dto.setPeakHour(stat.getPeakHour());
                    return dto;
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
    }

    /**
     * Get top 10 busiest time slots across all days
     */
    @Transactional
    public List<PeakHourDTO> getTopBusiestSlots() {
        List<PeakHourPrediction> predictions = predictionRepository
                .findTop10ByOrderByPredictedBookingsDesc();

        return predictions.stream()
                .map(this::mapToPeakHourDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get capacity utilization for today
     */
    @Transactional
    public Map<String, Object> getTodayCapacityUtilization() {
        LocalDate today = LocalDate.now();
        OffsetDateTime dayStart = today.atStartOfDay(ZoneId.of("Asia/Colombo")).toOffsetDateTime();
        OffsetDateTime dayEnd = dayStart.plusDays(1);

        List<Appointment> todayAppointments = appointmentRepository.findAll().stream()
                .filter(apt -> apt.getStartTs().isAfter(dayStart) &&
                        apt.getStartTs().isBefore(dayEnd) &&
                        ("BOOKED".equals(apt.getStatus()) ||
                                "CONFIRMED".equals(apt.getStatus())))
                .collect(Collectors.toList());

        // Calculate utilization by hour
        Map<Integer, Long> hourlyBookings = todayAppointments.stream()
                .collect(Collectors.groupingBy(
                        apt -> apt.getStartTs().getHour(),
                        Collectors.counting()
                ));

        // Get predictions for today
        int dayOfWeek = today.getDayOfWeek().getValue();
        List<PeakHourPrediction> predictions = predictionRepository.findByDayOfWeek(dayOfWeek);

        Map<String, Object> result = new HashMap<>();
        result.put("date", today);
        result.put("dayOfWeek", today.getDayOfWeek().name());
        result.put("actualBookings", hourlyBookings);
        result.put("predictions", predictions);
        result.put("totalBookingsToday", todayAppointments.size());

        return result;
    }

    /**
     * Force recalculation (for testing/admin)
     */
    @Transactional
    public String forceRecalculation() {
        System.out.println("Forcing analytics recalculation...");

        // Recalculate last 7 days
        for (int i = 1; i <= 7; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            OffsetDateTime dayStart = date.atStartOfDay(ZoneId.of("Asia/Colombo")).toOffsetDateTime();
            OffsetDateTime dayEnd = dayStart.plusDays(1);

            List<Appointment> appointments = appointmentRepository.findAll().stream()
                    .filter(apt -> apt.getStartTs().isAfter(dayStart) &&
                            apt.getStartTs().isBefore(dayEnd) &&
                            "COMPLETED".equals(apt.getStatus()))
                    .collect(Collectors.toList());

            if (!appointments.isEmpty()) {
                calculateDailyStats(date, appointments);
            }
        }

        // Recalculate predictions
        predictPeakHours();

        return "Analytics recalculation completed for last 7 days";
    }

    /**
     * Map entity to DTO
     */
    @Transactional
    private PeakHourDTO mapToPeakHourDTO(PeakHourPrediction prediction) {
        return new PeakHourDTO(
                prediction.getDayOfWeek(),
                prediction.getHourOfDay(),
                prediction.getPredictedBookings(),
                prediction.getConfidenceScore()
        );
    }
}