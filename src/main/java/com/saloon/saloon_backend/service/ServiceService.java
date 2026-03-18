package com.saloon.saloon_backend.service;

import com.saloon.saloon_backend.entity.SalonService;
import com.saloon.saloon_backend.repository.SalonServiceRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.springframework.stereotype.Service
public class ServiceService {

    private final SalonServiceRepository serviceRepository;

    public ServiceService(SalonServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Transactional
    public List<SalonService> getActiveServices() {
        return serviceRepository.findByIsActiveTrueOrderByNameAsc();
    }

    @Transactional
    public List<SalonService> getAllServices() {
        return serviceRepository.findAll();
    }
}
