package com.example.badminton.service;

import com.example.badminton.dto.response.BookingResponse;
import com.example.badminton.dto.response.RevenueReportResponse;
import com.example.badminton.entity.Booking;
import com.example.badminton.entity.BookingStatus;
import com.example.badminton.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    public List<BookingResponse> getBookings(LocalDate date, BookingStatus status) {
        return bookingRepository.findBookingsWithFilters(date, status)
                .stream()
                .filter(booking -> booking.getCourt() != null)
                .map(bookingService::toResponse)
                .collect(Collectors.toList());
    }

    public RevenueReportResponse getMonthlyRevenue(int year, int month) {
        List<Booking> confirmedBookings = bookingRepository.findConfirmedBookingsByMonth(year, month);

        BigDecimal totalRevenue = confirmedBookings.stream()
                .map(booking -> booking.getCourt().getPricePerHour())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalBookings = confirmedBookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .count();

        return RevenueReportResponse.builder()
                .year(year)
                .month(month)
                .totalConfirmedBookings(totalBookings)
                .totalRevenue(totalRevenue)
                .build();
    }
}
