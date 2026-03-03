// dto/BestTimeToBookDTO.java
package com.saloon.saloon_backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class BestTimeToBookDTO {
    private LocalDate date;
    private LocalTime time;
    private String dayName;
    private String reason;
    private Integer availableStylists;
    private String demandLevel;

    // Constructors
    public BestTimeToBookDTO() {}

    public BestTimeToBookDTO(LocalDate date, LocalTime time, String dayName, String reason, Integer availableStylists, String demandLevel) {
        this.date = date;
        this.time = time;
        this.dayName = dayName;
        this.reason = reason;
        this.availableStylists = availableStylists;
        this.demandLevel = demandLevel;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getAvailableStylists() {
        return availableStylists;
    }

    public void setAvailableStylists(Integer availableStylists) {
        this.availableStylists = availableStylists;
    }

    public String getDemandLevel() {
        return demandLevel;
    }

    public void setDemandLevel(String demandLevel) {
        this.demandLevel = demandLevel;
    }
}