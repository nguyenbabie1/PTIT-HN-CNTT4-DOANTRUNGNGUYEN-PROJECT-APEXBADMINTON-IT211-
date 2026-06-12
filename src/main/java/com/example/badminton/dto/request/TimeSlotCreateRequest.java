package com.example.badminton.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class TimeSlotCreateRequest {

    @NotBlank
    private String label;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;
}
