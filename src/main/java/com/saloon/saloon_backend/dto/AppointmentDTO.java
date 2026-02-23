package com.saloon.saloon_backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class AppointmentDTO {
    private Long id;
    private Long clientId;
    private String clientName;
    private Long stylistId;
    private String stylistName;
    private OffsetDateTime startTs;
    private OffsetDateTime endTs;
    private String status;
    private BigDecimal totalPrice;
    private String notes;
    private OffsetDateTime createdAt;
    private List<AppointmentItemDTO> items;
}