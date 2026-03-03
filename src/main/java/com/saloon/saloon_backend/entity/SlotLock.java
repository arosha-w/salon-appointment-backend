package com.saloon.saloon_backend.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "slot_locks")
public class SlotLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stylist_id", nullable = false)
    private Long stylistId;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "slot_time", nullable = false)
    private LocalTime slotTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "locked_at", nullable = false)
    private OffsetDateTime lockedAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Constructors
    public SlotLock() {
        this.lockedAt = OffsetDateTime.now();
        this.isActive = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStylistId() { return stylistId; }
    public void setStylistId(Long stylistId) { this.stylistId = stylistId; }

    public LocalDate getSlotDate() { return slotDate; }
    public void setSlotDate(LocalDate slotDate) { this.slotDate = slotDate; }

    public LocalTime getSlotTime() { return slotTime; }
    public void setSlotTime(LocalTime slotTime) { this.slotTime = slotTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public OffsetDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(OffsetDateTime lockedAt) { this.lockedAt = lockedAt; }

    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    // Helper
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }
}