package com.saloon.saloon_backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // client user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    // stylist user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stylist_id", nullable = false)
    private User stylist;

    @Column(name = "start_ts", nullable = false)
    private OffsetDateTime startTs;

    @Column(name = "end_ts", nullable = false)
    private OffsetDateTime endTs;

    @Column(name = "status", nullable = false)
    private String status = "BOOKED"; // BOOKED, CANCELLED, COMPLETED

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Appointment() {}

    public Long getId() { return id; }

    public User getClient() { return client; }
    public void setClient(User client) { this.client = client; }

    public User getStylist() { return stylist; }
    public void setStylist(User stylist) { this.stylist = stylist; }

    public OffsetDateTime getStartTs() { return startTs; }
    public void setStartTs(OffsetDateTime startTs) { this.startTs = startTs; }

    public OffsetDateTime getEndTs() { return endTs; }
    public void setEndTs(OffsetDateTime endTs) { this.endTs = endTs; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
