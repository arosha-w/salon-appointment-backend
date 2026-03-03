// dto/TimeSlotAvailabilityDTO.java
package com.saloon.saloon_backend.dto;

public class TimeSlotAvailabilityDTO {
    private String time;
    private Boolean isAvailable;
    private String demandLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private Integer currentBookings;
    private Integer totalCapacity;
    private Integer predictedBookings;
    private String recommendation; // "Best time", "Good choice", "Busy", "Very busy"

    // Constructors
    public TimeSlotAvailabilityDTO() {}

    // Getters and Setters
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public String getDemandLevel() {
        return demandLevel;
    }

    public void setDemandLevel(String demandLevel) {
        this.demandLevel = demandLevel;
    }

    public Integer getCurrentBookings() {
        return currentBookings;
    }

    public void setCurrentBookings(Integer currentBookings) {
        this.currentBookings = currentBookings;
    }

    public Integer getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(Integer totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public Integer getPredictedBookings() {
        return predictedBookings;
    }

    public void setPredictedBookings(Integer predictedBookings) {
        this.predictedBookings = predictedBookings;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}