package com.example.badminton.service;

import com.example.badminton.dto.request.BookingRequest;
import com.example.badminton.dto.response.BookingResponse;
import com.example.badminton.entity.BookingStatus;
import com.example.badminton.entity.Court;
import com.example.badminton.entity.TimeSlot;
import com.example.badminton.entity.User;
import com.example.badminton.exception.ConflictException;
import com.example.badminton.exception.ResourceNotFoundException;
import com.example.badminton.repository.BookingRepository;
import com.example.badminton.repository.CourtRepository;
import com.example.badminton.repository.TimeSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private Court court;
    private TimeSlot timeSlot;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("customer");
        user.setEnabled(true);

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
    }

    @Test
    void createBooking_success() {
        BookingRequest request = new BookingRequest();
        request.setCourtId(1L);
        request.setBookingDate(LocalDate.now());
        request.setTimeSlotId(1L);

        when(userService.findUserByUsername("customer")).thenReturn(user);
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));
        when(bookingRepository.existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusIn(
                anyLong(), any(), anyLong(), anyList())).thenReturn(false);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.createBooking("customer", request);

        assertNotNull(response);
        assertEquals(1L, response.getCourtId());
        assertEquals(BookingStatus.PENDING, response.getStatus());
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void createBooking_conflict_throwsException() {
        BookingRequest request = new BookingRequest();
        request.setCourtId(1L);
        request.setBookingDate(LocalDate.now());
        request.setTimeSlotId(1L);

        when(userService.findUserByUsername("customer")).thenReturn(user);
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));
        when(bookingRepository.existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusIn(
                anyLong(), any(), anyLong(), anyList())).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> bookingService.createBooking("customer", request));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_courtInactive_throwsException() {
        court.setActive(false);
        BookingRequest request = new BookingRequest();
        request.setCourtId(1L);
        request.setBookingDate(LocalDate.now());
        request.setTimeSlotId(1L);

        when(userService.findUserByUsername("customer")).thenReturn(user);
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));

        assertThrows(ConflictException.class,
                () -> bookingService.createBooking("customer", request));
        verify(bookingRepository, never()).save(any());
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
                .customerName("Customer")
                .build();

        when(userService.findUserByUsername("customer")).thenReturn(user);
        when(bookingRepository.findByUserId(1L)).thenReturn(List.of(
                com.example.badminton.entity.Booking.builder()
                        .id(1L)
                        .user(user)
                        .court(court)
                        .timeSlot(timeSlot)
                        .bookingDate(LocalDate.now())
                        .status(BookingStatus.PENDING)
                        .createdAt(java.time.LocalDateTime.now())
                        .build()
        ));

        List<BookingResponse> result = bookingService.getMyBookings("customer");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCourtId());
    }
}
