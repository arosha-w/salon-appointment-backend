package com.saloon.saloon_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientAppointmentHistoryDTO {
    private Long id;
    private String date;
    private String serviceName;
    private String stylistName;
    private Double amount;
    private String status;
}