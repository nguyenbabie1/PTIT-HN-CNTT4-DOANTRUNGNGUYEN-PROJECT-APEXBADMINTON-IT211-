package com.example.badminton.controller;

import com.example.badminton.dto.request.BookingRequest;
import com.example.badminton.dto.response.BookingResponse;
import com.example.badminton.entity.BookingStatus;
import com.example.badminton.entity.Court;
import com.example.badminton.entity.TimeSlot;
import com.example.badminton.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private BookingController bookingController;

    private Court court;
    private TimeSlot timeSlot;

    @BeforeEach
    void setUp() {
        court = new Court();
        court.setId(1L);
        court.setName("Court 1");
        court.setActive(true);
        court.setPricePerHour(new BigDecimal("100000"));

        timeSlot = new TimeSlot();
        timeSlot.setId(1L);
        timeSlot.setLabel("07:00-08:00");
        timeSlot.setStartTime(LocalTime.of(7, 0));
        timeSlot.setEndTime(LocalTime.of(8, 0));

        when(userDetails.getUsername()).thenReturn("testuser");
    }

    @Test
    void getMyBookings_success() {
        BookingResponse booking = BookingResponse.builder()
                .id(1L)
                .courtId(1L)
                .courtName("Court 1")
                .bookingDate(LocalDate.now())
                .timeSlotId(1L)
                .timeSlotLabel("07:00-08:00")
                .status(BookingStatus.PENDING)
                .customerName("Test Customer")
                .build();

        when(bookingService.getMyBookings("testuser"))
                .thenReturn(List.of(booking));

        ResponseEntity<List<BookingResponse>> result = bookingController.getMyBookings(userDetails);

        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(1L, result.getBody().get(0).getCourtId());
        assertEquals("Court 1", result.getBody().get(0).getCourtName());
        assertEquals(BookingStatus.PENDING, result.getBody().get(0).getStatus());
    }

    @Test
    void createBooking_success() {
        BookingRequest request = new BookingRequest();
        request.setCourtId(1L);
        request.setBookingDate(LocalDate.now());
        request.setTimeSlotId(1L);

        BookingResponse bookingResponse = BookingResponse.builder()
                .id(1L)
                .courtId(1L)
                .courtName("Court 1")
                .bookingDate(LocalDate.now())
                .timeSlotId(1L)
                .timeSlotLabel("07:00-08:00")
                .status(BookingStatus.PENDING)
                .customerName("Test Customer")
                .build();

        when(bookingService.createBooking(any(String.class), any(BookingRequest.class)))
                .thenReturn(bookingResponse);

        ResponseEntity<BookingResponse> result = bookingController.createBooking(userDetails, request);

        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
        assertEquals("Court 1", result.getBody().getCourtName());
        assertEquals(BookingStatus.PENDING, result.getBody().getStatus());
    }
}
