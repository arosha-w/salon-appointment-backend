package com.saloon.saloon_backend.repository;

import com.saloon.saloon_backend.entity.IdleCapacityAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IdleCapacityAlertRepository extends JpaRepository<IdleCapacityAlert, Long> {

    List<IdleCapacityAlert> findByIsResolvedFalseOrderByCreatedAtDesc();

    List<IdleCapacityAlert> findByAlertDateBetweenAndIsResolvedFalse(
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("SELECT a FROM IdleCapacityAlert a WHERE a.alertDate = ?1 " +
            "AND a.isResolved = false ORDER BY a.idlePercentage DESC")
    List<IdleCapacityAlert> findUnresolvedByDate(LocalDate date);

    @Query("SELECT a FROM IdleCapacityAlert a WHERE a.stylistId = ?1 " +
            "AND a.isResolved = false ORDER BY a.alertDate")
    List<IdleCapacityAlert> findUnresolvedByStylist(Long stylistId);
}