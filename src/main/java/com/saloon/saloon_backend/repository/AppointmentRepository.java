package com.saloon.saloon_backend.repository;

import com.saloon.saloon_backend.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // ==================== ADMIN QUERIES ====================

    @Query("SELECT a FROM Appointment a ORDER BY a.startTs DESC")
    List<Appointment> findAllOrderByStartTsDesc();

    //  FIXED: Use CAST and CURRENT_DATE without DATE() function
    @Query("SELECT a FROM Appointment a WHERE CAST(a.startTs AS date) = CURRENT_DATE ORDER BY a.startTs ASC")
    List<Appointment> findTodayAppointments();

    @Query("SELECT COUNT(a) FROM Appointment a WHERE CAST(a.startTs AS date) = CURRENT_DATE")
    Integer countTodayAppointments();

    @Query("SELECT COUNT(a) FROM Appointment a WHERE CAST(a.startTs AS date) = CURRENT_DATE AND a.status = :status")
    Integer countTodayAppointmentsByStatus(@Param("status") String status);

    @Query("SELECT a FROM Appointment a WHERE a.startTs BETWEEN :start AND :end ORDER BY a.startTs DESC")
    List<Appointment> findByDateRange(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    //  FIXED: Use CAST and CURRENT_DATE
    @Query("SELECT COALESCE(SUM(a.totalPrice), 0) FROM Appointment a WHERE CAST(a.startTs AS date) = CURRENT_DATE AND a.status = 'COMPLETED'")
    BigDecimal getTodayRevenue();

    @Query("SELECT COALESCE(SUM(a.totalPrice), 0) FROM Appointment a WHERE a.startTs >= :start AND a.startTs < :end AND a.status = 'COMPLETED'")
    BigDecimal getRevenueByDateRange(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    // ==================== CLIENT QUERIES ====================

    @Query("SELECT a FROM Appointment a WHERE a.client.id = :clientId ORDER BY a.startTs DESC")
    List<Appointment> findByClientId(@Param("clientId") Long clientId);

    @Query("SELECT a FROM Appointment a WHERE a.client.id = :clientId AND a.status = :status ORDER BY a.startTs DESC")
    List<Appointment> findByClientIdAndStatus(@Param("clientId") Long clientId, @Param("status") String status);

    @Query("SELECT a FROM Appointment a WHERE a.client.id = :clientId AND a.startTs > :now AND a.status IN ('BOOKED', 'CONFIRMED') ORDER BY a.startTs ASC")
    List<Appointment> findUpcomingByClientId(@Param("clientId") Long clientId, @Param("now") OffsetDateTime now);

    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.items WHERE a.client.id = :clientId AND a.endTs < :now ORDER BY a.startTs DESC")
    List<Appointment> findPastByClientId(@Param("clientId") Long clientId, @Param("now") OffsetDateTime now);

    // ==================== STYLIST QUERIES ====================

    @Query("SELECT a FROM Appointment a WHERE a.stylist.id = :stylistId AND a.startTs >= :start AND a.endTs <= :end AND a.status IN ('BOOKED', 'CONFIRMED') ORDER BY a.startTs ASC")
    List<Appointment> findByStylistAndDateRange(
            @Param("stylistId") Long stylistId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    //  ADDED: count bookings for capacity monitoring (this fixes your error)
    @Query("""
        SELECT COUNT(a)
        FROM Appointment a
        WHERE a.stylist.id = :stylistId
          AND a.startTs >= :start
          AND a.startTs < :end
          AND a.status IN ('BOOKED','CONFIRMED')
    """)
    long countBookingsForStylistInTimeRange(
            @Param("stylistId") Long stylistId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    // ==================== OVERLAP CHECK ====================

    @Query("""
        SELECT COUNT(a) > 0 FROM Appointment a
        WHERE a.stylist.id = :stylistId
          AND a.status NOT IN ('CANCELLED')
          AND (a.startTs < :endTs AND a.endTs > :startTs)
    """)
    boolean existsOverlap(
            @Param("stylistId") Long stylistId,
            @Param("startTs") OffsetDateTime startTs,
            @Param("endTs") OffsetDateTime endTs
    );
}