package com.example.badminton.service;

import com.example.badminton.dto.response.CourtImageResponse;
import com.example.badminton.dto.response.FileUploadResponse;
import com.example.badminton.entity.*;
import com.example.badminton.exception.BadRequestException;
import com.example.badminton.exception.ResourceNotFoundException;
import com.example.badminton.repository.BookingRepository;
import com.example.badminton.repository.CourtImageRepository;
import com.example.badminton.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png");

    private final CloudStorageService cloudStorageService;
    private final CourtRepository courtRepository;
    private final CourtImageRepository courtImageRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;

    @Transactional
    public FileUploadResponse upload(
            MultipartFile file,
            UploadEntityType entityType,
            Long entityId,
            UserDetails userDetails) {

        validateFile(file);

        if (entityType == null || entityId == null) {
            String url = cloudStorageService.upload(file);
            return FileUploadResponse.builder().url(url).build();
        }

        return switch (entityType) {
            case COURT -> uploadCourtImage(file, entityId, userDetails);
            case BOOKING -> uploadBookingBill(file, entityId, userDetails);
        };
    }

    private FileUploadResponse uploadCourtImage(MultipartFile file, Long courtId, UserDetails userDetails) {
        requireRole(userDetails, "ROLE_MANAGER", "ROLE_ADMIN");

        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found"));

        String url = cloudStorageService.upload(file);

        CourtImage courtImage = CourtImage.builder()
                .court(court)
                .imageUrl(url)
                .createdAt(LocalDateTime.now())
                .build();

        CourtImage saved = courtImageRepository.save(courtImage);

        return FileUploadResponse.builder()
                .url(url)
                .imageId(saved.getId())
                .entityType(UploadEntityType.COURT.name())
                .entityId(courtId)
                .build();
    }

    private FileUploadResponse uploadBookingBill(MultipartFile file, Long bookingId, UserDetails userDetails) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (hasRole(userDetails, "ROLE_CUSTOMER")) {
            User currentUser = userService.findUserByUsername(userDetails.getUsername());
            if (!booking.getUser().getId().equals(currentUser.getId())) {
                throw new BadRequestException("You can only upload bill for your own booking");
            }
        } else if (!hasRole(userDetails, "ROLE_MANAGER", "ROLE_ADMIN")) {
            throw new BadRequestException("Insufficient permissions to upload booking bill");
        }

        String url = cloudStorageService.upload(file);
        booking.setBillImageUrl(url);
        bookingRepository.save(booking);

        return FileUploadResponse.builder()
                .url(url)
                .entityType(UploadEntityType.BOOKING.name())
                .entityId(bookingId)
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size must be less than 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Only PNG and JPG images are allowed");
        }
    }

    private void requireRole(UserDetails userDetails, String... roles) {
        if (!hasRole(userDetails, roles)) {
            throw new BadRequestException("Insufficient permissions for this upload");
        }
    }

    private boolean hasRole(UserDetails userDetails, String... roles) {
        if (userDetails == null) {
            return false;
        }
        List<String> required = List.of(roles);
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(required::contains);
    }
}
