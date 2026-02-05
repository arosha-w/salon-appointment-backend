package com.saloon.saloon_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "appointment_items")
public class AppointmentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private SalonService service;

    @Column(name = "duration_min", nullable = false)
    private Integer durationMin;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public AppointmentItem() {}

    public Long getId() { return id; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public SalonService getService() { return service; }
    public void setService(SalonService service) { this.service = service; }

    public Integer getDurationMin() { return durationMin; }
    public void setDurationMin(Integer durationMin) { this.durationMin = durationMin; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
