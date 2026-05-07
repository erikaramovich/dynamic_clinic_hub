package com.miro.project.controller;

import com.miro.project.dto.request.AppointmentRequest;
import com.miro.project.model.Appointment;
import com.miro.project.model.AppointmentStatus;
import com.miro.project.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService service;

    private UUID getAuthenticatedUserId() {
        return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Appointment> create(@RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(service.createAppointment(request, getAuthenticatedUserId()));
    }


    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public Page<Appointment> getMy(Pageable pageable) {
        return service.getPatientAppointments(getAuthenticatedUserId(), pageable);
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public void assign(@PathVariable UUID id, @RequestParam UUID doctorId) {
        service.assignDoctor(id, doctorId);
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('DOCTOR')")
    public void complete(@PathVariable UUID id) {
        service.updateStatus(id, AppointmentStatus.COMPLETED);
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public Page<Appointment> getAll(Pageable pageable) {
        return service.getDoctorAppointments(getAuthenticatedUserId(), pageable);
    }


    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public Page<Appointment> getAll(@RequestParam(required = false) AppointmentStatus status, Pageable pageable) {
        if (status != null) {
            return service.getAppointmentsByStatus(status, pageable);
        }
        return service.getAll(pageable);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMINISTRATOR', 'DOCTOR')")
    public void cancel(@PathVariable UUID id) {
        service.updateStatus(id, AppointmentStatus.CANCELLED);
    }
}