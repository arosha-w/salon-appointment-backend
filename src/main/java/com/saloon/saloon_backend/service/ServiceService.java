package com.saloon.saloon_backend.service;

import com.saloon.saloon_backend.entity.Service;
import com.saloon.saloon_backend.repository.ServiceRepository;

import java.util.List;

@org.springframework.stereotype.Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<Service> getActiveServices() {
        return serviceRepository.findByIsActiveTrueOrderByNameAsc();
    }

    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }
}
