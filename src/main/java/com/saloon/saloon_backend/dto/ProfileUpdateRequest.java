package com.saloon.saloon_backend.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
}