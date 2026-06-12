package com.example.badminton.service;

import com.example.badminton.dto.request.LoginRequest;
import com.example.badminton.dto.request.RefreshTokenRequest;
import com.example.badminton.dto.response.AuthResponse;
import com.example.badminton.entity.User;
import com.example.badminton.entity.Role;
import com.example.badminton.security.JwtService;
import com.example.badminton.repository.TokenBlacklistRepository;
import com.example.badminton.repository.UserRepository;
import com.example.badminton.repository.PasswordResetTokenRepository;
import com.example.badminton.entity.PasswordResetToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password("encoded")
                .roles("CUSTOMER")
                .build();
    }

    @Test
    void refresh_validToken_returnsNewTokens() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        when(jwtService.isRefreshToken("valid-refresh-token")).thenReturn(true);
        when(jwtService.extractUsername("valid-refresh-token")).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.isTokenValid(anyString(), any(UserDetails.class))).thenReturn(true);
        when(jwtService.generateAccessToken(any(UserDetails.class))).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(any(UserDetails.class))).thenReturn("new-refresh-token");
        when(jwtService.getRemainingTimeMs(anyString())).thenReturn(1800L);

        AuthResponse response = authService.refresh(request);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        verify(jwtService, times(1)).generateAccessToken(userDetails);
        verify(jwtService, times(1)).generateRefreshToken(userDetails);
    }

    @Test
    void refresh_invalidToken_throwsException() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-token");

        when(jwtService.isRefreshToken("invalid-token")).thenReturn(false);

        assertThrows(com.example.badminton.exception.BadRequestException.class,
                () -> authService.refresh(request));
    }
}
