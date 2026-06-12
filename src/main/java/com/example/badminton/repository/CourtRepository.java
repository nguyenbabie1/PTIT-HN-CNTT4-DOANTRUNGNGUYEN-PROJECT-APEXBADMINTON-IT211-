package com.example.badminton.repository;

import com.example.badminton.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CourtRepository extends JpaRepository<Court, Long> {

    boolean existsByName(String name);

    @Query("SELECT c FROM Court c WHERE c.active = true AND c.id NOT IN "
            + "(SELECT b.court.id FROM Booking b WHERE b.bookingDate = :date "
            + "AND b.status IN ('PENDING','CONFIRMED'))")
    List<Court> findAvailableCourtsByDate(@Param("date") LocalDate date);
}
