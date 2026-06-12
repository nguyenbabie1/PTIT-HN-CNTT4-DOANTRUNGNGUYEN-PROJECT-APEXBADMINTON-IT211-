package com.example.badminton.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class TimeSlotResponse {

    private Long id;
    private String label;
    private LocalTime startTime;
    private LocalTime endTime;
}
