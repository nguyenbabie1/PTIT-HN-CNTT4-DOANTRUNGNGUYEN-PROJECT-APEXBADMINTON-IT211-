package com.example.badminton.controller;

import com.example.badminton.dto.response.CourtResponse;
import com.example.badminton.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    @GetMapping
    public ResponseEntity<List<CourtResponse>> getCourts() {
        return ResponseEntity.ok(courtService.getAllCourts());
    }
}
