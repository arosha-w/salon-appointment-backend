package com.saloon.saloon_backend.dto;

public class LoginResponse {
    private String token;
    private String role;

    public LoginResponse(String token, String role) {
        this.token = token;
        this.role = String.valueOf(role);
    }

    public String getToken() { return token; }
    public String getRole() { return role; }
}
