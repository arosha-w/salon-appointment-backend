package com.saloon.saloon_backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdminStylistDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String[] specialties;
    private Integer experienceYears;
    private String bio;
    private BigDecimal rating;
    private Integer totalReviews;
    private Integer totalClients;
    private BigDecimal totalRevenue;
    private Boolean isAvailable;
    private String status;
}