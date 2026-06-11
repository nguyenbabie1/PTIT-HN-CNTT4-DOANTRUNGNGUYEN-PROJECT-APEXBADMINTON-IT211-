package com.example.badminton.dto.response;

import com.example.badminton.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long courtId;
    private String courtName;
    private LocalDate bookingDate;
    private Long timeSlotId;
    private String timeSlotLabel;
    private BookingStatus status;
    private String customerName;
    private String billImageUrl;
}
