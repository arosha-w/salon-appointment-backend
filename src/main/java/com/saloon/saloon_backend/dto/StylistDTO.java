package com.saloon.saloon_backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StylistDTO {
    private Long id;
    private String name;
    private String email;
    private String[] specialties;
    private Integer experienceYears;
    private String bio;
    private BigDecimal rating;
    private Integer totalReviews;
    private Boolean isAvailable;
}