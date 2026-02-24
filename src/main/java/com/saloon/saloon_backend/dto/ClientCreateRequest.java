package com.saloon.saloon_backend.dto;

import lombok.Data;

@Data
public class ClientCreateRequest {
    private String fullName;
    private String email;
    private String phone;
    private String password;
}