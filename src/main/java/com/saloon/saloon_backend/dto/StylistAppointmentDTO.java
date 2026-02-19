package com.saloon.saloon_backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class StylistAppointmentDTO {
    private Long id;
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private List<ServiceItemDTO> services;
    private OffsetDateTime startTs;
    private OffsetDateTime endTs;
    private String status;
    private BigDecimal totalPrice;
    private String notes;
    private OffsetDateTime createdAt;

    @Data
    public static class ServiceItemDTO {
        private Long serviceId;
        private String serviceName;
        private Integer durationMin;
        private BigDecimal price;
    }
}