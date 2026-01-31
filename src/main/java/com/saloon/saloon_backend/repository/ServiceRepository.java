package com.saloon.saloon_backend.repository;

import com.saloon.saloon_backend.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Long> {
}
