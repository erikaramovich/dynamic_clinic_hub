package com.miro.project.service;

import com.miro.project.dto.request.LoginRequest;
import com.miro.project.dto.request.RegisterRequest;
import com.miro.project.dto.response.AuthResponse;
import com.miro.project.model.RefreshToken;
import com.miro.project.model.User;
import com.miro.project.repository.RefreshTokenRepository;
import com.miro.project.repository.UserRepository;
import com.miro.project.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Check if name or email already exists
        if (userRepository.existsByName(request.getName())) {
            throw new RuntimeException("Name is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // 2. Build and save the new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                // Never store plain text passwords! Hash it via BCrypt.
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        user = userRepository.save(user);

        // 3. Generate tokens for the newly registered user
        return createAuthSession(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 1. Find user by name
        User user = userRepository.findByName(request.getName())
                .orElseThrow(() -> new RuntimeException("Invalid name or password"));

        // 2. Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid name or password");
        }

        // 3. Generate tokens
        return createAuthSession(user);
    }

    // Helper method to generate Access and Refresh tokens
    private AuthResponse createAuthSession(User user) {
        // Generate JWT
        String accessToken = jwtProvider.generateAccessToken(user.getName(), user.getRole());

        // Create Refresh Token (expires in 7 days)
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        // Delete old refresh tokens for this user so they don't pile up in the DB
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.save(refreshToken);

        // Build Response DTO
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
}