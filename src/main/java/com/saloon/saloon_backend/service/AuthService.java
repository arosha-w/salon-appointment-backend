package com.saloon.saloon_backend.service;

import com.saloon.saloon_backend.dto.LoginRequest;
import com.saloon.saloon_backend.dto.LoginResponse;
import com.saloon.saloon_backend.entity.User;
import com.saloon.saloon_backend.repository.UserRepository;
import com.saloon.saloon_backend.service.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());

        // ✅ FIXED: Return all required fields
        return new LoginResponse(
                token,
                user.getRole().name(),
                user.getId(),
                user.getFullName(),
                user.getEmail()
        );
    }
}