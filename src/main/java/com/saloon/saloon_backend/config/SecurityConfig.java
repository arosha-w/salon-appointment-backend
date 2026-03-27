package com.saloon.saloon_backend.config;

import com.saloon.saloon_backend.security.JwtAuthFilter;
import com.saloon.saloon_backend.service.CustomUserDetailsService;
import com.saloon.saloon_backend.service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(userDetailsService, jwtService);

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public routes (no authentication needed)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/services/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stylists/**").permitAll()

                        // Admin routes (ADMIN role only)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Stylist routes (STYLIST role only)
                        .requestMatchers("/api/stylist/**").hasRole("STYLIST")

                        // Client routes (CLIENT role only)
                        .requestMatchers("/api/client/**").hasRole("CLIENT")

                        // Appointment routes (CLIENT, STYLIST, or ADMIN)
                        .requestMatchers("/api/appointments/**").hasAnyRole("CLIENT", "STYLIST", "ADMIN")

                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}