package com.example.badminton.service;

import com.example.badminton.dto.request.CourtCreateRequest;
import com.example.badminton.dto.request.CourtUpdateRequest;
import com.example.badminton.dto.response.CourtImageResponse;
import com.example.badminton.dto.response.CourtResponse;
import com.example.badminton.entity.Court;
import com.example.badminton.exception.BadRequestException;
import com.example.badminton.exception.ResourceNotFoundException;
import com.example.badminton.repository.BookingRepository;
import com.example.badminton.repository.CourtImageRepository;
import com.example.badminton.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourtService {

    private final CourtRepository courtRepository;
    private final CourtImageRepository courtImageRepository;
    private final CourtImageService courtImageService;
    private final BookingRepository bookingRepository;

    public List<CourtResponse> getAllCourts() {
        return courtRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CourtResponse> getAvailableCourtsByDate(LocalDate date) {
        return courtRepository.findAvailableCourtsByDate(date).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CourtResponse getCourtById(Long id) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found"));
        return toResponse(court);
    }

    @Transactional
    public CourtResponse createCourt(CourtCreateRequest request) {
        if (courtRepository.existsByName(request.getName())) {
            throw new BadRequestException("Court name already exists");
        }
        Court court = Court.builder()
                .name(request.getName())
                .pricePerHour(request.getPricePerHour())
                .active(request.isActive())
                .build();
        return toResponse(courtRepository.save(court));
    }

    @Transactional
    public CourtResponse updateCourt(Long id, CourtUpdateRequest request) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            if (!court.getName().equals(request.getName()) && courtRepository.existsByName(request.getName())) {
                throw new BadRequestException("Court name already exists");
            }
            court.setName(request.getName());
        }
        if (request.getPricePerHour() != null) {
            court.setPricePerHour(request.getPricePerHour());
        }
        if (request.getActive() != null) {
            court.setActive(request.getActive());
        }

        return toResponse(courtRepository.save(court));
    }

    @Transactional
    public void deleteCourt(Long id) {
        if (!courtRepository.existsById(id)) {
            throw new ResourceNotFoundException("Court not found");
        }
        courtRepository.deleteById(id);
    }

    private CourtResponse toResponse(Court court) {
        List<CourtImageResponse> images = courtImageRepository
                .findByCourtIdOrderByCreatedAtDesc(court.getId()).stream()
                .map(courtImageService::toResponse)
                .collect(Collectors.toList());
        return CourtResponse.builder()
                .id(court.getId())
                .name(court.getName())
                .active(court.isActive())
                .pricePerHour(court.getPricePerHour())
                .images(images)
                .build();
    }
}
