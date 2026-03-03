package com.saloon.saloon_backend.dto;

public class SlotLockResponseDTO {
    private Long lockId;
    private String sessionId;
    private String expiresAt;
    private Integer secondsRemaining;
    private Boolean success;
    private String message;

    // Constructors
    public SlotLockResponseDTO() {}

    // Getters and Setters
    public Long getLockId() { return lockId; }
    public void setLockId(Long lockId) { this.lockId = lockId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public Integer getSecondsRemaining() { return secondsRemaining; }
    public void setSecondsRemaining(Integer secondsRemaining) {
        this.secondsRemaining = secondsRemaining;
    }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}