package com.example.badminton.controller;

import com.example.badminton.dto.response.FileUploadResponse;
import com.example.badminton.entity.UploadEntityType;
import com.example.badminton.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER', 'ADMIN')")
    public ResponseEntity<FileUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "entityType", required = false) UploadEntityType entityType,
            @RequestParam(value = "entityId", required = false) Long entityId,
            @AuthenticationPrincipal UserDetails userDetails) {
        FileUploadResponse response = fileService.upload(file, entityType, entityId, userDetails);
        return ResponseEntity.ok(response);
    }
}
