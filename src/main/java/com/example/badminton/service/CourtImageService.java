package com.example.badminton.service;

import com.example.badminton.dto.response.CourtImageResponse;
import com.example.badminton.entity.CourtImage;
import com.example.badminton.exception.ResourceNotFoundException;
import com.example.badminton.repository.CourtImageRepository;
import com.example.badminton.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourtImageService {

    private final CourtImageRepository courtImageRepository;
    private final CourtRepository courtRepository;

    public List<CourtImageResponse> getCourtImages(Long courtId) {
        if (!courtRepository.existsById(courtId)) {
            throw new ResourceNotFoundException("Court not found");
        }
        return courtImageRepository.findByCourtIdOrderByCreatedAtDesc(courtId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCourtImage(Long courtId, Long imageId) {
        if (!courtImageRepository.existsByIdAndCourtId(imageId, courtId)) {
            throw new ResourceNotFoundException("Court image not found");
        }
        courtImageRepository.deleteById(imageId);
    }

    public CourtImageResponse toResponse(CourtImage image) {
        return CourtImageResponse.builder()
                .id(image.getId())
                .courtId(image.getCourt().getId())
                .imageUrl(image.getImageUrl())
                .createdAt(image.getCreatedAt())
                .build();
    }
}
