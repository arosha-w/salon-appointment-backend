// entity/SlotConfiguration.java
package com.saloon.saloon_backend.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "slot_configurations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"day_of_week", "hour_of_day"}))
public class SlotConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1-7

    @Column(name = "hour_of_day", nullable = false)
    private Integer hourOfDay; // 0-23

    @Column(name = "base_capacity")
    private Integer baseCapacity = 4; // Default: 4 stylists can handle 4 appointments per hour

    @Column(name = "peak_capacity")
    private Integer peakCapacity = 6; // During peaks, can handle 6

    @Column(name = "is_peak_hour")
    private Boolean isPeakHour = false;

    @Column(name = "slot_duration_min")
    private Integer slotDurationMin = 30; // 15, 30, or 60 minutes

    // Constructors
    public SlotConfiguration() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(Integer hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public Integer getBaseCapacity() {
        return baseCapacity;
    }

    public void setBaseCapacity(Integer baseCapacity) {
        this.baseCapacity = baseCapacity;
    }

    public Integer getPeakCapacity() {
        return peakCapacity;
    }

    public void setPeakCapacity(Integer peakCapacity) {
        this.peakCapacity = peakCapacity;
    }

    public Boolean getIsPeakHour() {
        return isPeakHour;
    }

    public void setIsPeakHour(Boolean isPeakHour) {
        this.isPeakHour = isPeakHour;
    }

    public Integer getSlotDurationMin() {
        return slotDurationMin;
    }

    public void setSlotDurationMin(Integer slotDurationMin) {
        this.slotDurationMin = slotDurationMin;
    }
}