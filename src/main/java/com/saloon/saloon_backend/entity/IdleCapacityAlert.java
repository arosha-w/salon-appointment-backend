package com.saloon.saloon_backend.entity;

import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;

@Entity
@Table(name = "idle_capacity_alerts")
public class IdleCapacityAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_date", nullable = false)
    private LocalDate alertDate;

    @Column(name = "alert_hour", nullable = false)
    private Integer alertHour; // 0-23

    @Column(name = "stylist_id")
    private Long stylistId;

    @Column(name = "expected_bookings", nullable = false)
    private Integer expectedBookings;

    @Column(name = "actual_bookings", nullable = false)
    private Integer actualBookings;

    @Column(name = "idle_percentage", precision = 5, scale = 2)
    private BigDecimal idlePercentage;

    @Column(name = "revenue_loss_estimate", precision = 10, scale = 2)
    private BigDecimal revenueLossEstimate;

    @Column(name = "alert_level")
    private String alertLevel; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "is_resolved")
    private Boolean isResolved = false;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    // Constructor
    public IdleCapacityAlert() {
        this.createdAt = OffsetDateTime.now();
        this.isResolved = false;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getAlertDate() { return alertDate; }
    public void setAlertDate(LocalDate alertDate) { this.alertDate = alertDate; }

    public Integer getAlertHour() { return alertHour; }
    public void setAlertHour(Integer alertHour) { this.alertHour = alertHour; }

    public Long getStylistId() { return stylistId; }
    public void setStylistId(Long stylistId) { this.stylistId = stylistId; }

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

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}










































































































































































































































