package com.example.badminton.service;

import com.example.badminton.dto.response.CourtImageResponse;
import com.example.badminton.entity.Court;
import com.example.badminton.entity.CourtImage;
import com.example.badminton.exception.BadRequestException;
import com.example.badminton.exception.ResourceNotFoundException;
import com.example.badminton.repository.CourtImageRepository;
import com.example.badminton.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourtImageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/jpg", "image/png");

    private final CourtImageRepository courtImageRepository;
    private final CourtRepository courtRepository;
    private final CloudStorageService cloudStorageService;

    public List<CourtImageResponse> getCourtImages(Long courtId) {
        if (!courtRepository.existsById(courtId)) {
            throw new ResourceNotFoundException("Court not found");
        }
        return courtImageRepository.findByCourtIdOrderByCreatedAtDesc(courtId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<CourtImageResponse> uploadImages(Long courtId, List<MultipartFile> files) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found"));

        if (files == null || files.isEmpty()) {
            throw new BadRequestException("No files provided");
        }

        List<CourtImageResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            validateFile(file);

            String imageUrl = cloudStorageService.upload(file);

            CourtImage courtImage = CourtImage.builder()
                    .court(court)
                    .imageUrl(imageUrl)
                    .build();

            CourtImage saved = courtImageRepository.save(courtImage);
            responses.add(toResponse(saved));
        }

        return responses;
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds 5MB limit");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only PNG and JPG images are allowed");
        }
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
