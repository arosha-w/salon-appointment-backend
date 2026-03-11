package com.saloon.saloon_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "daily_stats")
public class DailyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_date", nullable = false, unique = true)
    private LocalDate statDate;

    @Column(name = "total_appointments")
    private Integer totalAppointments = 0;

    @Column(name = "confirmed_appointments")
    private Integer confirmedAppointments = 0;

    @Column(name = "completed_appointments")
    private Integer completedAppointments = 0;

    @Column(name = "cancelled_appointments")
    private Integer cancelledAppointments = 0;

    @Column(name = "avg_appointment_value", precision = 10, scale = 2)
    private BigDecimal avgAppointmentValue = BigDecimal.ZERO;

    @Column(name = "total_revenue", precision = 10, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "new_clients")
    private Integer newClients = 0;

    @Column(name = "returning_clients")
    private Integer returningClients = 0;

    @Column(name = "peak_hour")
    private Integer peakHour;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();

}