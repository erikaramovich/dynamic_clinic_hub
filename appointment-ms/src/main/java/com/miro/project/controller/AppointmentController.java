package com.miro.project.controller;

import com.miro.project.dto.request.AppointmentRequest;
import com.miro.project.dto.response.AppointmentResponse;
import com.miro.project.dto.response.UserInternalResponse;
import com.miro.project.model.Appointment;
import com.miro.project.model.AppointmentStatus;
import com.miro.project.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService service;

    private UUID getAuthenticatedUserId() {
        return (UUID) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }

    // Support for Available Doctors List
    @GetMapping("/doctors")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMINISTRATOR')")
    public List<UserInternalResponse> listDoctors() {
        return service.getAvailableDoctors();
    }

    // NEW: Use Google Calendar Service to fetch slots for a doctor
    @GetMapping("/slots")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<Instant>> getSlots(@RequestParam String doctorName,
                                                  @RequestParam Instant start,
                                                  @RequestParam Instant end) {
        // Controller remains clean and works with the internal system IDs
        return ResponseEntity.ok(service.getAvailableSlots(doctorName, start, end));
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentRequest request) {
        Appointment app = service.createAppointment(request, getAuthenticatedUserId());
        return ResponseEntity.ok(convertToResponse(app));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public Page<AppointmentResponse> getMy(Pageable pageable) {
        return service.getPatientAppointments(getAuthenticatedUserId(), pageable).map(this::convertToResponse);
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> assign(@PathVariable UUID id, @RequestParam String doctorName) {
        service.assignDoctor(id, doctorName);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> complete(@PathVariable UUID id) {
        service.updateStatus(id, AppointmentStatus.COMPLETED);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public Page<AppointmentResponse> getAll(@RequestParam(required = false) AppointmentStatus status, Pageable pageable) {
        Page<Appointment> result = (status != null) ? service.getAppointmentsByStatus(status, pageable) : service.getAll(pageable);
        return result.map(this::convertToResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMINISTRATOR', 'DOCTOR')")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        String role = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getAuthorities().iterator().next().getAuthority();
        service.cancelAppointment(id, getAuthenticatedUserId(), role);
        return ResponseEntity.noContent().build();
    }

    private AppointmentResponse convertToResponse(Appointment app) {
        return AppointmentResponse.builder()
                .id(app.getId())
                .patientId(app.getPatientId())
                .doctorId(app.getDoctorId())
                .appointmentTime(app.getAppointmentTime())
                .status(app.getStatus())
                .build();
    }
}