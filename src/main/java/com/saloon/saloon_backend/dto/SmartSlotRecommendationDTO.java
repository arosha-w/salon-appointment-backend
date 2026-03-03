package com.saloon.saloon_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

public class SmartSlotRecommendationDTO {
    private Long stylistId;
    private String stylistName;
    private LocalDate recommendedDate;
    private LocalTime recommendedTime;
    private String formattedDateTime; // For display
    private BigDecimal score; // 0-100
    private String reason;
    private String demandLevel; // LOW, MEDIUM, HIGH
    private Integer availableCapacity;
    private Map<String, Object> factors;

    // Constructors
    public SmartSlotRecommendationDTO() {}

    // Getters and Setters
    public Long getStylistId() { return stylistId; }
    public void setStylistId(Long stylistId) { this.stylistId = stylistId; }

    public String getStylistName() { return stylistName; }
    public void setStylistName(String stylistName) {
        this.stylistName = stylistName;
    }

    public LocalDate getRecommendedDate() { return recommendedDate; }
    public void setRecommendedDate(LocalDate recommendedDate) {
        this.recommendedDate = recommendedDate;
    }

    public LocalTime getRecommendedTime() { return recommendedTime; }
    public void setRecommendedTime(LocalTime recommendedTime) {
        this.recommendedTime = recommendedTime;
    }

    public String getFormattedDateTime() { return formattedDateTime; }
    public void setFormattedDateTime(String formattedDateTime) {
        this.formattedDateTime = formattedDateTime;
    }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDemandLevel() { return demandLevel; }
    public void setDemandLevel(String demandLevel) {
        this.demandLevel = demandLevel;
    }

    public Integer getAvailableCapacity() { return availableCapacity; }
    public void setAvailableCapacity(Integer availableCapacity) {
        this.availableCapacity = availableCapacity;
    }

    public Map<String, Object> getFactors() { return factors; }
    public void setFactors(Map<String, Object> factors) {
        this.factors = factors;
    }
}