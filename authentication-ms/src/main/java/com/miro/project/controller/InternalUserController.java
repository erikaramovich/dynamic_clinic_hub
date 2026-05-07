package com.miro.project.controller;

import com.miro.project.dto.response.UserInternalResponse;
import com.miro.project.model.Role;
import com.miro.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserRepository userRepository;

    // Fetch all users with DOCTOR role
    @GetMapping("/doctors")
    public List<UserInternalResponse> getAllDoctors() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.DOCTOR)
                .map(this::mapToInternal)
                .collect(Collectors.toList());
    }

    // Resolve name to internal details
    @GetMapping("/search")
    public ResponseEntity<UserInternalResponse> findDoctorByName(@RequestParam String name) {
        return userRepository.findByNameAndRole(name, Role.DOCTOR)
                .map(u -> ResponseEntity.ok(mapToInternal(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    private UserInternalResponse mapToInternal(com.miro.project.model.User user) {
        return UserInternalResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}