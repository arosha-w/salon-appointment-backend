package com.saloon.saloon_backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class AppointmentHistoryDTO {
    private Long id;
    private OffsetDateTime date;
    private String serviceName;
    private String stylistName;
    private BigDecimal amount;
    private String status;
    private List<String> serviceNames;
}