package com.example.projects.airBnbApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelReportDto {
    private Long bookingsCount;
    private BigDecimal totalRevenue;
    private BigDecimal avgRevenue;
}
