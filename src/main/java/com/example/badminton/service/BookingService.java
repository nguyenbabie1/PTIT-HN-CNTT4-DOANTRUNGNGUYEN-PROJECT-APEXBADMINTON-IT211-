package com.example.badminton.service;

import com.example.badminton.dto.request.BookingRequest;
import com.example.badminton.dto.request.BookingStatusUpdateRequest;
import com.example.badminton.dto.response.BookingResponse;
import com.example.badminton.entity.*;
import com.example.badminton.exception.BadRequestException;
import com.example.badminton.exception.ConflictException;
import com.example.badminton.exception.ResourceNotFoundException;
import com.example.badminton.repository.BookingRepository;
import com.example.badminton.repository.CourtRepository;
import com.example.badminton.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserService userService;

    @Transactional
    public BookingResponse createBooking(String username, BookingRequest request) {
        User user = userService.findUserByUsername(username);

        if (!user.isEnabled()) {
            throw new ConflictException("Account is locked");
        }

        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court not found"));

        if (!court.isActive()) {
            throw new ConflictException("Court is not available");
        }

        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found"));

        boolean conflict = bookingRepository.existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusIn(
                request.getCourtId(),
                request.getBookingDate(),
                request.getTimeSlotId(),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));

        if (conflict) {
            throw new ConflictException("Time slot already booked");
        }

        Booking booking = Booking.builder()
                .user(user)
                .court(court)
                .timeSlot(timeSlot)
                .bookingDate(request.getBookingDate())
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(bookingRepository.save(booking));
    }

    public List<BookingResponse> getMyBookings(String username) {
        User user = userService.findUserByUsername(username);
        return bookingRepository.findByUserId(user.getId()).stream()
                .map(this::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public BookingResponse updateBookingStatus(Long bookingId, BookingStatusUpdateRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only pending bookings can be approved or rejected");
        }

        if (request.getStatus() != BookingStatus.CONFIRMED && request.getStatus() != BookingStatus.REJECTED) {
            throw new BadRequestException("Status must be CONFIRMED or REJECTED");
        }

        if (request.getStatus() == BookingStatus.CONFIRMED) {
            boolean conflict = bookingRepository.existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusAndIdNot(
                    booking.getCourt().getId(),
                    booking.getBookingDate(),
                    booking.getTimeSlot().getId(),
                    BookingStatus.CONFIRMED,
                    bookingId);
            if (conflict) {
                throw new ConflictException("Time slot already confirmed by another booking");
            }
        }

        booking.setStatus(request.getStatus());
        return toResponse(bookingRepository.save(booking));
    }

    public List<BookingResponse> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    public BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .courtId(booking.getCourt().getId())
                .courtName(booking.getCourt().getName())
                .bookingDate(booking.getBookingDate())
                .timeSlotId(booking.getTimeSlot().getId())
                .timeSlotLabel(booking.getTimeSlot().getLabel())
                .status(booking.getStatus())
                .customerName(booking.getUser().getFullName())
                .billImageUrl(booking.getBillImageUrl())
                .build();
    }
}
