package com.saloon.saloon_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class IdleCapacityAlertDTO {
    private Long id;
    private LocalDate alertDate;
    private Integer alertHour;
    private String timeSlot; // e.g., "2:00 PM - 3:00 PM"
    private Long stylistId;
    private String stylistName;
    private Integer expectedBookings;
    private Integer actualBookings;
    private BigDecimal idlePercentage;
    private BigDecimal revenueLossEstimate;
    private String alertLevel;
    private Boolean isResolved;

    // Constructors
    public IdleCapacityAlertDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getAlertDate() { return alertDate; }
    public void setAlertDate(LocalDate alertDate) { this.alertDate = alertDate; }

    public Integer getAlertHour() { return alertHour; }
    public void setAlertHour(Integer alertHour) { this.alertHour = alertHour; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public Long getStylistId() { return stylistId; }
    public void setStylistId(Long stylistId) { this.stylistId = stylistId; }

    public String getStylistName() { return stylistName; }
    public void setStylistName(String stylistName) {
        this.stylistName = stylistName;
    }

    public Integer getExpectedBookings() { return expectedBookings; }
    public void setExpectedBookings(Integer expectedBookings) {
        this.expectedBookings = expectedBookings;
    }

    public Integer getActualBookings() { return actualBookings; }
    public void setActualBookings(Integer actualBookings) {
        this.actualBookings = actualBookings;
    }

    public BigDecimal getIdlePercentage() { return idlePercentage; }
    public void setIdlePercentage(BigDecimal idlePercentage) {
        this.idlePercentage = idlePercentage;
    }

    public BigDecimal getRevenueLossEstimate() { return revenueLossEstimate; }
    public void setRevenueLossEstimate(BigDecimal revenueLossEstimate) {
        this.revenueLossEstimate = revenueLossEstimate;
    }

    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }

    public Boolean getIsResolved() { return isResolved; }
    public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }
}