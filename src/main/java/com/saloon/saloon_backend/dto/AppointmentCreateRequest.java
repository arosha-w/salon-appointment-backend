package com.saloon.saloon_backend.dto;

import java.util.List;

public class AppointmentCreateRequest {
    public Long stylistId;
    public String startTs;          // ISO string e.g. "2026-02-05T10:00:00+05:30"
    public List<Long> serviceIds;   // [1,2,3]
}
