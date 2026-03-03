package com.saloon.saloon_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppointmentCreateResponse {
    private Long appointmentId;
    private String startTs;
    private String endTs;
    private String status;
}