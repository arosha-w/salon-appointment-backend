package com.saloon.saloon_backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AppointmentItemDTO {
    private Long serviceId;
    private String serviceName;
    private Integer durationMin;
    private BigDecimal price;
}