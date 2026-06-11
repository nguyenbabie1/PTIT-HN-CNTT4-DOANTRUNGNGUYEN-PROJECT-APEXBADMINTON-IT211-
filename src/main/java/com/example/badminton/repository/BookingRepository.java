package com.example.badminton.repository;

import com.example.badminton.entity.Booking;
import com.example.badminton.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusIn(
            Long courtId, LocalDate bookingDate, Long timeSlotId, List<BookingStatus> statuses);

    boolean existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusAndIdNot(
            Long courtId, LocalDate bookingDate, Long timeSlotId, BookingStatus status, Long excludeId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.court JOIN FETCH b.timeSlot JOIN FETCH b.user " +
           "WHERE b.user.id = :userId")
    List<Booking> findByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.court JOIN FETCH b.timeSlot JOIN FETCH b.user " +
           "WHERE (:date IS NULL OR b.bookingDate = :date) " +
           "AND (:status IS NULL OR b.status = :status)")
    List<Booking> findBookingsWithFilters(@Param("date") LocalDate date,
                                          @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b JOIN FETCH b.court " +
           "WHERE b.status = com.example.badminton.entity.BookingStatus.CONFIRMED " +
           "AND YEAR(b.bookingDate) = :year AND MONTH(b.bookingDate) = :month")
    List<Booking> findConfirmedBookingsByMonth(@Param("year") int year, @Param("month") int month);
}
