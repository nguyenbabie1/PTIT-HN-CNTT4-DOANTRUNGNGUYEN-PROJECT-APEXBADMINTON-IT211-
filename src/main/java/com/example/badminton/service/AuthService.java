package com.example.badminton.service;

import com.example.badminton.dto.request.ChangePasswordRequest;
import com.example.badminton.dto.request.ForgotPasswordRequest;
import com.example.badminton.dto.request.LoginRequest;
import com.example.badminton.dto.request.RefreshTokenRequest;
import com.example.badminton.dto.request.ResetPasswordRequest;
import com.example.badminton.dto.response.ApiResponse;
import com.example.badminton.dto.response.AuthResponse;
import com.example.badminton.dto.response.ForgotPasswordResponse;
import com.example.badminton.entity.PasswordResetToken;
import com.example.badminton.entity.TokenBlacklist;
import com.example.badminton.entity.User;
import com.example.badminton.exception.BadRequestException;
import com.example.badminton.repository.PasswordResetTokenRepository;
import com.example.badminton.repository.TokenBlacklistRepository;
import com.example.badminton.repository.UserRepository;
import com.example.badminton.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${password-reset.expiration-ms}")
    private long passwordResetExpirationMs;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        return buildAuthResponse(userDetails);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new BadRequestException("Refresh token expired or invalid");
        }

        return buildAuthResponse(userDetails);
    }

    @Transactional
    public void logout(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new BadRequestException("Access token is required");
        }

        if (!jwtService.isAccessToken(accessToken)) {
            throw new BadRequestException("Invalid access token");
        }

        Date expiration = jwtService.extractExpiration(accessToken);
        tokenBlacklistRepository.save(TokenBlacklist.builder()
                .token(accessToken)
                .expiresAt(expiration.toInstant())
                .build());
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String[] resetToken = {null};
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            resetToken[0] = UUID.randomUUID().toString();
            passwordResetTokenRepository.save(PasswordResetToken.builder()
                    .token(resetToken[0])
                    .user(user)
                    .expiresAt(Instant.now().plusMillis(passwordResetExpirationMs))
                    .used(false)
                    .build());

            log.info("===== PASSWORD RESET TOKEN =====");
            log.info("Email: {}", request.getEmail());
            log.info("Token: {}", resetToken[0]);
            log.info("================================");
        });

        return ForgotPasswordResponse.builder()
                .message("If the email exists, a password reset link has been sent")
                .resetToken(resetToken[0])
                .build();
    }

    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return new ApiResponse("Password reset successful");
    }

    private AuthResponse buildAuthResponse(UserDetails userDetails) {
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getRemainingTimeMs(accessToken) / 1000)
                .build();
    }
}
