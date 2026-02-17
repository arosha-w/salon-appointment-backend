package com.saloon.saloon_backend.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptPasswordGenerator {
    public static void main(String[] args) {
        // Example raw password you want to hash
        String rawPassword = "TestPassword123!";

        // BCryptPasswordEncoder to encode the password
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(rawPassword);

        // Print the bcrypt encoded password to be used in the database
        System.out.println("BCrypt Hashed Password: " + hashedPassword);
    }
}

