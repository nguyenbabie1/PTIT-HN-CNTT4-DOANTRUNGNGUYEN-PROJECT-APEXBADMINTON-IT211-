package com.example.badminton.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CourtResponse {

    private Long id;
    private String name;
    private boolean active;
    private BigDecimal pricePerHour;
    private List<CourtImageResponse> images;
}
