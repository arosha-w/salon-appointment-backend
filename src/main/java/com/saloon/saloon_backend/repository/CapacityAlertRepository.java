// repository/CapacityAlertRepository.java
package com.saloon.saloon_backend.repository;

import com.saloon.saloon_backend.entity.CapacityAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CapacityAlertRepository extends JpaRepository<CapacityAlert, Long> {

    //  ADDED (this fixes your error)
    List<CapacityAlert> findByIsResolvedFalseOrderByCreatedAtDesc();

    // Existing methods (kept as-is)
    List<CapacityAlert> findByIsResolvedFalseOrderBySeverityDescCreatedAtDesc();

    List<CapacityAlert> findByAlertDateAndIsResolvedFalse(LocalDate date);

    @Query("SELECT COUNT(ca) > 0 FROM CapacityAlert ca " +
            "WHERE ca.alertDate = :date " +
            "AND ca.alertTime = :time " +
            "AND ca.stylist.id = :stylistId " +
            "AND ca.alertType = :type " +
            "AND ca.isResolved = false")
    boolean existsByDateAndTimeAndStylistAndTypeAndResolved(
            @Param("date") LocalDate date,
            @Param("time") LocalTime time,
            @Param("stylistId") Long stylistId,
            @Param("type") String type
    );

    List<CapacityAlert> findTop10ByIsResolvedFalseOrderByCreatedAtDesc();

    @Query("SELECT ca FROM CapacityAlert ca " +
            "WHERE ca.isResolved = false " +
            "ORDER BY " +
            "CASE ca.severity " +
            "  WHEN 'CRITICAL' THEN 1 " +
            "  WHEN 'HIGH' THEN 2 " +
            "  WHEN 'MEDIUM' THEN 3 " +
            "  WHEN 'LOW' THEN 4 " +
            "  ELSE 5 " +
            "END, " +
            "ca.createdAt DESC")
    List<CapacityAlert> findActiveAlertsSortedBySeverity();
}