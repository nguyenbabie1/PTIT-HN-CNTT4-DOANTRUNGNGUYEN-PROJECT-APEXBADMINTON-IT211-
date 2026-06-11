package com.example.badminton.repository;

import com.example.badminton.entity.CourtImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourtImageRepository extends JpaRepository<CourtImage, Long> {

    List<CourtImage> findByCourtIdOrderByCreatedAtDesc(Long courtId);

    boolean existsByIdAndCourtId(Long id, Long courtId);
}
