package com.miro.project.controller;

import com.miro.project.dto.request.LoginRequest;
import com.miro.project.dto.request.RegisterRequest;
import com.miro.project.dto.response.AuthResponse;
import com.miro.project.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("[REGISTER] request: {}", request.toString());
        AuthResponse response = authService.register(request);
        log.info("[REGISTER] response: {}", response.toString());
        return createCookieResponse(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("[LOGIN] request: {}", request.toString());
        AuthResponse response = authService.login(request);
        log.info("[LOGIN] response: {}", response.toString());
        return createCookieResponse(response);
    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@CookieValue(name = "refreshToken") String refreshToken) {
        // Calls the service to validate the token and generate a new JWT
        AuthResponse newTokens = authService.refreshToken(refreshToken);

        // We only return the access token and user info to the frontend body.
        // The refresh token remains secure in the cookie.
        AuthResponse responseBody = AuthResponse.builder()
                .accessToken(newTokens.getAccessToken())
                .name(newTokens.getName())
                .role(newTokens.getRole())
                .build();

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        // 1. Delete the token from the PostgreSQL database
        if (refreshToken != null && !refreshToken.isEmpty()) {
            authService.logout(refreshToken);
        }

        // 2. Create an empty cookie that expires immediately to overwrite the existing cookie in the user's browser
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // Set to true in production if using HTTPS
                .path("/api/auth/refresh")
                .maxAge(0) // 0 means delete immediately
                .sameSite("Strict")
                .build();

        // 3. Return a successful response with the cleared cookie
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body("Logged out successfully");
    }

    // Helper method: You should also update your register() and login() endpoints
    // in AuthController to use a helper like this, so they also issue the token as a cookie!
    private ResponseEntity<AuthResponse> createCookieResponse(AuthResponse tokens) {
        ResponseCookie springCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)       // JavaScript cannot read this cookie
                .secure(false)        // Set to true if using HTTPS in prod
                .path("/api/auth/refresh") // Cookie is only sent to the refresh endpoint
                .maxAge(7 * 24 * 60 * 60) // 7 days in seconds
                .sameSite("Strict")   // Prevents CSRF attacks
                .build();

        AuthResponse responseBody = AuthResponse.builder()
                .accessToken(tokens.getAccessToken())
                .name(tokens.getName())
                .role(tokens.getRole())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, springCookie.toString())
                .body(responseBody);
    }
}