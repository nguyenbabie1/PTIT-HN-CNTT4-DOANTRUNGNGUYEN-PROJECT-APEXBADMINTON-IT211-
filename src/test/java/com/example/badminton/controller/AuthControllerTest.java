package com.example.badminton.controller;

import com.example.badminton.dto.request.LoginRequest;
import com.example.badminton.dto.request.ForgotPasswordRequest;
import com.example.badminton.dto.response.AuthResponse;
import com.example.badminton.dto.response.ForgotPasswordResponse;
import com.example.badminton.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .tokenType("Bearer")
                .expiresIn(1800)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertNotNull(response.getBody());
        assertEquals("test-access-token", response.getBody().getAccessToken());
        assertEquals("test-refresh-token", response.getBody().getRefreshToken());
        assertEquals("Bearer", response.getBody().getTokenType());
    }

    @Test
    void forgotPassword_success() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        ForgotPasswordResponse forgotPasswordResponse = ForgotPasswordResponse.builder()
                .message("If the email exists, a password reset link has been sent")
                .resetToken("test-reset-token")
                .build();

        when(authService.forgotPassword(any(ForgotPasswordRequest.class)))
                .thenReturn(forgotPasswordResponse);

        ResponseEntity<ForgotPasswordResponse> response = authController.forgotPassword(request);

        assertNotNull(response.getBody());
        assertEquals("If the email exists, a password reset link has been sent", response.getBody().getMessage());
        assertEquals("test-reset-token", response.getBody().getResetToken());
    }
}
