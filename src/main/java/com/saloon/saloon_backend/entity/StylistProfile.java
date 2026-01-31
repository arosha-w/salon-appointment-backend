package com.saloon.saloon_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stylist_profiles")
public class StylistProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "specialties")
    private String specialties;

    @Column(name = "buffer_min", nullable = false)
    private Integer bufferMin = 0;

    public StylistProfile() {}

    public Long getUserId() { return userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSpecialties() { return specialties; }
    public void setSpecialties(String specialties) {
        this.specialties = specialties;
    }

    public Integer getBufferMin() { return bufferMin; }
    public void setBufferMin(Integer bufferMin) {
        this.bufferMin = bufferMin;
    }
}
