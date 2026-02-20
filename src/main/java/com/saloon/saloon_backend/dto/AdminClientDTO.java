package com.saloon.saloon_backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class AdminClientDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Integer totalVisits;
    private BigDecimal totalSpent;
    private OffsetDateTime lastVisit;
    private String status;
    private String membershipTier;
    private OffsetDateTime createdAt;
}