package com.example.badminton.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ForgotPasswordResponse {
    private String message;
    private String resetToken;
}
