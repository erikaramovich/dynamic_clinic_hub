package com.miro.project.service;

import com.miro.project.dto.response.UserInternalResponse;
import com.miro.project.exception.DoctorNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorServiceClient {
    private final RestClient authWebClient;

    @Cacheable(value = "doctors")
    @CircuitBreaker(name = "authService")
    public List<UserInternalResponse> getAvailableDoctors() {
        return authWebClient.get()
                .uri("/api/internal/users/doctors")
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {
                });
    }

    @Cacheable(value = "doctors", key = "#name")
    @CircuitBreaker(name = "authService")
    public UserInternalResponse resolveDoctorByName(String name) {
        return authWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/internal/users/search")
                        .queryParam("name", name)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new DoctorNotFoundException("Doctor with name '" + name + "' not found.");
                })
                .body(UserInternalResponse.class);
    }
}