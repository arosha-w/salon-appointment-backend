// dto/PeakHourDTO.java
package com.saloon.saloon_backend.dto;

import java.math.BigDecimal;

public class PeakHourDTO {
    private Integer dayOfWeek;
    private String dayName;
    private Integer hourOfDay;
    private String timeSlot;
    private Integer predictedBookings;
    private BigDecimal confidenceScore;
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    public PeakHourDTO() {}

    public PeakHourDTO(Integer dayOfWeek, Integer hourOfDay, Integer predictedBookings, BigDecimal confidenceScore) {
        this.dayOfWeek = dayOfWeek;
        this.hourOfDay = hourOfDay;
        this.predictedBookings = predictedBookings;
        this.confidenceScore = confidenceScore;
        this.dayName = getDayNameFromNumber(dayOfWeek);
        this.timeSlot = formatTimeSlot(hourOfDay);
        this.riskLevel = calculateRiskLevel(predictedBookings);
    }

    private String getDayNameFromNumber(Integer day) {
        String[] days = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        return day >= 1 && day <= 7 ? days[day] : "Unknown";
    }

    private String formatTimeSlot(Integer hour) {
        int endHour = hour + 1;
        return String.format("%02d:00 - %02d:00", hour, endHour);
    }

    private String calculateRiskLevel(Integer bookings) {
        if (bookings >= 10) return "CRITICAL";
        if (bookings >= 7) return "HIGH";
        if (bookings >= 4) return "MEDIUM";
        return "LOW";
    }

    // Getters and Setters
    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getDayName() { return dayName; }
    public void setDayName(String dayName) { this.dayName = dayName; }

    public Integer getHourOfDay() { return hourOfDay; }
    public void setHourOfDay(Integer hourOfDay) { this.hourOfDay = hourOfDay; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public Integer getPredictedBookings() { return predictedBookings; }
    public void setPredictedBookings(Integer predictedBookings) { this.predictedBookings = predictedBookings; }

    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
}