package com.saloon.saloon_backend.dto;

import lombok.Data;

@Data
public class StylistCreateRequest {
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String[] specialties;
    private Integer experienceYears;
    private String bio;
}