package com.example.badminton.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class RevenueReportResponse {

    private int year;
    private int month;
    private long totalConfirmedBookings;
    private BigDecimal totalRevenue;
}
