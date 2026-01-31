package com.saloon.saloon_backend.controller;

import com.saloon.saloon_backend.entity.Service;
import com.saloon.saloon_backend.service.ServiceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    // For client booking UI (active only)
    @GetMapping
    public List<Service> getActiveServices() {
        return serviceService.getActiveServices();
    }

    // For admin dashboard (all)
    @GetMapping("/all")
    public List<Service> getAllServices() {
        return serviceService.getAllServices();
    }
}
