package com.saloon.saloon_backend.dto;

public class AppointmentCreateResponse {
    public Long appointmentId;
    public String startTs;
    public String endTs;
    public String status;

    public AppointmentCreateResponse(Long appointmentId, String startTs, String endTs, String status) {
        this.appointmentId = appointmentId;
        this.startTs = startTs;
        this.endTs = endTs;
        this.status = status;
    }
}
