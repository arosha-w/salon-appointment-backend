package com.saloon.saloon_backend.dto;

import lombok.Data;

@Data
public class ClientProfileDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String memberSince;
    private Integer totalVisits;
    private Integer loyaltyPoints;
    private String membershipTier;
}