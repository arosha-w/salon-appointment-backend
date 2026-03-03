package com.saloon.saloon_backend.dto;

public class SlotLockRequestDTO {
    private Long stylistId;
    private String date; // YYYY-MM-DD
    private String time; // HH:mm
    private Integer durationMinutes;
    private String sessionId;

    // Constructors
    public SlotLockRequestDTO() {}

    // Getters and Setters
    public Long getStylistId() { return stylistId; }
    public void setStylistId(Long stylistId) { this.stylistId = stylistId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
