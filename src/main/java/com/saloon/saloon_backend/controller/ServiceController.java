package com.saloon.saloon_backend.controller;

import com.saloon.saloon_backend.dto.ServiceDTO;
import com.saloon.saloon_backend.entity.SalonService;
import com.saloon.saloon_backend.service.ServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "http://localhost:3000")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceDTO>> getActiveServices() {
        List<SalonService> services = serviceService.getActiveServices();

        List<ServiceDTO> dtos = services.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ServiceDTO>> getAllServices() {
        List<SalonService> services = serviceService.getAllServices();

        List<ServiceDTO> dtos = services.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    private ServiceDTO mapToDTO(SalonService service) {
        ServiceDTO dto = new ServiceDTO();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setDurationMin(service.getDefaultDurationMin());
        dto.setPrice(service.getBasePrice());
        dto.setCategory(service.getCategory());
        return dto;
    }
}