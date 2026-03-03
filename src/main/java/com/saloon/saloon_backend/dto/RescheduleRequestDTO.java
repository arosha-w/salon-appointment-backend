// ============================================================================
// FILE: dto/RescheduleRequestDTO.java (FIXED VERSION)
// ============================================================================
package com.saloon.saloon_backend.dto;

public class RescheduleRequestDTO {
    private Long appointmentId;
    private Long newStylistId;
    private Long stylistId; // Alternate field name
    private String newStartTs;  // ISO format: "2026-03-10T10:00:00+05:30"
    private String newStartTime; // Alternate field name
    private String reason;

    // Constructors
    public RescheduleRequestDTO() {}

    // Getters and Setters
    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getNewStylistId() {
        // Return newStylistId if available, otherwise stylistId
        return newStylistId != null ? newStylistId : stylistId;
    }

    public void setNewStylistId(Long newStylistId) {
        this.newStylistId = newStylistId;
    }

    public Long getStylistId() {
        return stylistId;
    }

    public void setStylistId(Long stylistId) {
        this.stylistId = stylistId;
    }

    public String getNewStartTs() {
        // Return newStartTs if available, otherwise newStartTime
        return newStartTs != null ? newStartTs : newStartTime;
    }

    public void setNewStartTs(String newStartTs) {
        this.newStartTs = newStartTs;
    }

    public String getNewStartTime() {
        return newStartTime;
    }

    public void setNewStartTime(String newStartTime) {
        this.newStartTime = newStartTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}