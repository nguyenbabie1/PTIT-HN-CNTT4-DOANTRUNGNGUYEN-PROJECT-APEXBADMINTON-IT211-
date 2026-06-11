package com.example.badminton.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FileUploadResponse {

    private String url;
    private Long imageId;
    private String entityType;
    private Long entityId;
}
