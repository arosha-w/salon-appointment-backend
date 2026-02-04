package com.saloon.saloon_backend.controller;

import com.saloon.saloon_backend.dto.LoginRequest;
import com.saloon.saloon_backend.dto.LoginResponse;
import com.saloon.saloon_backend.security.JwtUtil;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email, request.password)
        );

        String token = JwtUtil.generateToken(request.email);
        return new LoginResponse(token);
    }
}
