package com.saloon.saloon_backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdminServiceDTO {
    private Long id;
    private String name;
    private String description;
    private Integer durationMin;
    private BigDecimal price;
    private String category;
    private Boolean isActive;
    private Integer totalBookings;
    private BigDecimal totalRevenue;
}