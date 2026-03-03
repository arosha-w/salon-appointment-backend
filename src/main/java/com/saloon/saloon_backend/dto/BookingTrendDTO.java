// dto/BookingTrendDTO.java
package com.saloon.saloon_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingTrendDTO {
    private LocalDate date;
    private String dateFormatted;
    private Integer totalBookings;
    private BigDecimal totalRevenue;
    private Integer peakHour;
    private String trend; // INCREASING, DECREASING, STABLE

    public BookingTrendDTO() {}

    public BookingTrendDTO(LocalDate date, Integer totalBookings, BigDecimal totalRevenue) {
        this.date = date;
        this.totalBookings = totalBookings;
        this.totalRevenue = totalRevenue;
        this.dateFormatted = date.toString();
    }

    // Getters and Setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDateFormatted() { return dateFormatted; }
    public void setDateFormatted(String dateFormatted) { this.dateFormatted = dateFormatted; }

    public Integer getTotalBookings() { return totalBookings; }
    public void setTotalBookings(Integer totalBookings) { this.totalBookings = totalBookings; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public Integer getPeakHour() { return peakHour; }
    public void setPeakHour(Integer peakHour) { this.peakHour = peakHour; }

    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
}