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
        log.info("Register request: {}", request.getUserFullInfo());
        AuthResponse response = authService.register(request);
        log.info("Register response: {}", response.getUserFullInfo());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request: {}", request.getUserFullInfo());
        AuthResponse response = authService.login(request);
        log.info("Login response: {}", response.getUserFullInfo());
        return ResponseEntity.ok(response);
    }
}