package com.example.badminton.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CourtImageResponse {

    private Long id;
    private Long courtId;
    private String imageUrl;
    private LocalDateTime createdAt;
}
