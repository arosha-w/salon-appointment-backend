package com.saloon.saloon_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AdminDashboardStatsDTO {
    private Integer totalAppointments;
    private Integer activeStylists;
    private Integer totalClients;
    private BigDecimal todayRevenue;
    private String appointmentChange;
    private String stylistChange;
    private String clientChange;
    private String revenueChange;
}