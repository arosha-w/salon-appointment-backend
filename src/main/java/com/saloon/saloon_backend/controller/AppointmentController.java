package com.saloon.saloon_backend.controller;

import com.saloon.saloon_backend.dto.AppointmentCreateRequest;
import com.saloon.saloon_backend.dto.AppointmentCreateResponse;
import com.saloon.saloon_backend.service.AppointmentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public AppointmentCreateResponse create(Authentication authentication,
                                            @RequestBody AppointmentCreateRequest req) {

        // authentication.getName() = email (from JWT subject)
        String clientEmail = authentication.getName();
        return appointmentService.createAppointment(clientEmail, req);
    }
}
