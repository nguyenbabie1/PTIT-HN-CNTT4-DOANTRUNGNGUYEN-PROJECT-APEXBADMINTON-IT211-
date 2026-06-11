package com.example.badminton.controller;

import com.example.badminton.dto.response.CourtImageResponse;
import com.example.badminton.service.CourtImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courts/{courtId}/images")
@RequiredArgsConstructor
public class CourtImageController {

    private final CourtImageService courtImageService;

    @GetMapping
    public ResponseEntity<List<CourtImageResponse>> getCourtImages(@PathVariable Long courtId) {
        return ResponseEntity.ok(courtImageService.getCourtImages(courtId));
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteCourtImage(
            @PathVariable Long courtId,
            @PathVariable Long imageId) {
        courtImageService.deleteCourtImage(courtId, imageId);
        return ResponseEntity.noContent().build();
    }
}
