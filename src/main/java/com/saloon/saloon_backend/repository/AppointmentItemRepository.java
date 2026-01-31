package com.saloon.saloon_backend.repository;

import com.saloon.saloon_backend.entity.AppointmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentItemRepository
        extends JpaRepository<AppointmentItem, Long> {
}
