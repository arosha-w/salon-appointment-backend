package com.saloon.saloon_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppointmentStatsDTO {
    private Integer todayTotal;
    private Integer confirmed;
    private Integer pending;
    private Integer cancelled;
}