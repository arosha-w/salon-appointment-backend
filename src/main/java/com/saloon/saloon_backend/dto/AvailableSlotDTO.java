package com.saloon.saloon_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor  // ✅ ADD THIS
@AllArgsConstructor
public class AvailableSlotDTO {
    private String time;
    private String timestamp;
    private Boolean available;
}