package com.example.badminton.dto.request;

import com.example.badminton.entity.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingStatusUpdateRequest {

    @NotNull
    private BookingStatus status;
}
