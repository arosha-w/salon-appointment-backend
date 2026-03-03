package com.saloon.saloon_backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class AppointmentCreateRequest {
    private Long stylistId;
    private String startTs;
    private List<Long> serviceIds;
}