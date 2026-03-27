package com.saloon.saloon_backend.dto;

public class ClientAppointmentHistoryDTO {
    private Long id;
    private String date;
    private String serviceName;
    private String stylistName;
    private Long stylistId; //  ADD THIS for rebook functionality
    private Double amount;
    private String status;

    // Constructors
    public ClientAppointmentHistoryDTO() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getStylistName() {
        return stylistName;
    }

    public void setStylistName(String stylistName) {
        this.stylistName = stylistName;
    }

    public Long getStylistId() {
        return stylistId;
    }

    public void setStylistId(Long stylistId) {
        this.stylistId = stylistId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}




































































































































































































































