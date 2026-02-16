package com.saloon.saloon_backend.repository;

import com.saloon.saloon_backend.entity.SalonService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalonServiceRepository extends JpaRepository<SalonService, Long> {
    List<SalonService> findByIsActiveTrueOrderByNameAsc();
    List<SalonService> findByIdIn(List<Long> ids);
}
