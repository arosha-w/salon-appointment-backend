package com.saloon.saloon_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StylistDashboardStatsDTO {
    private Integer todayAppointments;
    private Integer pendingConfirmations;
    private Integer completedToday;
    private BigDecimal earningsToday;
    private Integer totalClients;
    private BigDecimal weeklyEarnings;
}