package com.saloon.saloon_backend.repository;

import com.saloon.saloon_backend.entity.StylistProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StylistProfileRepository extends JpaRepository<StylistProfile, Long> {

    Optional<StylistProfile> findByUserId(Long userId);

    @Query("SELECT sp FROM StylistProfile sp WHERE sp.isAvailable = true ORDER BY sp.rating DESC")
    List<StylistProfile> findAvailableStylists();
}