package com.saloon.saloon_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ClientStatsDTO {
    private Integer totalVisits;
    private BigDecimal totalSpent;
    private Integer loyaltyPoints;
    private String memberSince;
}