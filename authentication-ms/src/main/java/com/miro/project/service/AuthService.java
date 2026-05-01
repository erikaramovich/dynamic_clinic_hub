package com.miro.project.service;

import com.miro.project.dto.request.LoginRequest;
import com.miro.project.dto.request.RegisterRequest;
import com.miro.project.dto.response.AuthResponse;
import com.miro.project.exception.TokenRefreshException;
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

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String requestRefreshToken) {
        // 1. Find the refresh token in the database
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException("Refresh token is not in database!"));

        // 2. Check if the refresh token has expired (e.g., older than 7 days)
        if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            // If expired, delete it from the DB and force the user to login with a password again
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException("Refresh token was expired. Please make a new sign in request.");
        }

        // 3. Token is valid! Get the user associated with this token
        User user = refreshToken.getUser();

        // 4. Generate a brand new short-lived Access Token
        String newAccessToken = jwtProvider.generateAccessToken(user.getName(), user.getRole());

        // 5. Return the new Access Token (we keep the same refresh token)
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public void logout(String requestRefreshToken) {
        // Find and delete the refresh token from the database.
        // This permanently revokes the user's ability to get new access tokens.
        refreshTokenRepository.deleteByToken(requestRefreshToken);
    }
}