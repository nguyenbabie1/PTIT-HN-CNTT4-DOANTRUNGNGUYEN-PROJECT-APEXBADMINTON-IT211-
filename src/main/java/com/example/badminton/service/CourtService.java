package com.example.badminton.service;

import com.example.badminton.dto.response.CourtImageResponse;
import com.example.badminton.dto.response.CourtResponse;
import com.example.badminton.repository.CourtImageRepository;
import com.example.badminton.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourtService {

    private final CourtRepository courtRepository;
    private final CourtImageRepository courtImageRepository;
    private final CourtImageService courtImageService;

    public List<CourtResponse> getAllCourts() {
        return courtRepository.findAll().stream()
                .map(court -> {
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
                })
                .collect(Collectors.toList());
    }

    public CourtResponse getCourtById(Long id) {
        return courtRepository.findById(id)
                .map(court -> CourtResponse.builder()
                        .id(court.getId())
                        .name(court.getName())
                        .active(court.isActive())
                        .pricePerHour(court.getPricePerHour())
                        .images(courtImageRepository.findByCourtIdOrderByCreatedAtDesc(id).stream()
                                .map(courtImageService::toResponse)
                                .collect(Collectors.toList()))
                        .build())
                .orElseThrow(() -> new com.example.badminton.exception.ResourceNotFoundException("Court not found"));
    }
}
