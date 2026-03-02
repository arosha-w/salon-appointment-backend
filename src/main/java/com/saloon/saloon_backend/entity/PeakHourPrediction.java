// entity/PeakHourPrediction.java
package com.saloon.saloon_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "peak_hour_predictions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"day_of_week", "hour_of_day"}))
public class PeakHourPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "hour_of_day", nullable = false)
    private Integer hourOfDay;

    @Column(name = "predicted_bookings", nullable = false)
    private Integer predictedBookings;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "prediction_type", length = 50)
    private String predictionType = "HISTORICAL_AVERAGE";

    @Column(name = "last_calculated")
    private OffsetDateTime lastCalculated = OffsetDateTime.now();

    // Constructors
    public PeakHourPrediction() {}

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

    public Integer getPredictedBookings() {
        return predictedBookings;
    }

    public void setPredictedBookings(Integer predictedBookings) {
        this.predictedBookings = predictedBookings;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getPredictionType() {
        return predictionType;
    }

    public void setPredictionType(String predictionType) {
        this.predictionType = predictionType;
    }

    public OffsetDateTime getLastCalculated() {
        return lastCalculated;
    }

    public void setLastCalculated(OffsetDateTime lastCalculated) {
        this.lastCalculated = lastCalculated;
    }
}