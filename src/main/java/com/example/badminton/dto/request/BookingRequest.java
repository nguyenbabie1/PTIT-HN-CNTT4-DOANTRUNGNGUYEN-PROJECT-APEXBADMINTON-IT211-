package com.example.badminton.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookingRequest {

    @NotNull
    private Long courtId;

    @NotNull
    @FutureOrPresent
    private LocalDate bookingDate;

    @NotNull
    private Long timeSlotId;
}
