package com.example.badminton.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CourtCreateRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal pricePerHour;

    private boolean active = true;
}
