package com.saloon.saloon_backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "stylist_time_off")
public class StylistTimeOff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stylist_id", nullable = false)
    private User stylist;

    @Column(name = "start_ts", nullable = false)
    private OffsetDateTime startTs;

    @Column(name = "end_ts", nullable = false)
    private OffsetDateTime endTs;

    @Column(name = "reason")
    private String reason;

    public StylistTimeOff() {}

    public Long getId() { return id; }

    public User getStylist() { return stylist; }
    public void setStylist(User stylist) { this.stylist = stylist; }

    public OffsetDateTime getStartTs() { return startTs; }
    public void setStartTs(OffsetDateTime startTs) {
        this.startTs = startTs;
    }

    public OffsetDateTime getEndTs() { return endTs; }
    public void setEndTs(OffsetDateTime endTs) {
        this.endTs = endTs;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
