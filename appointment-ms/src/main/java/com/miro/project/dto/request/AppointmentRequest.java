package com.miro.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class AppointmentRequest {
    @Schema(description = "The Name of the doctor you want to see", example = "Hovo")
    private String doctorName;

    @NotNull(message = "Appointment time is required")
    @Future(message = "Appointment must be scheduled for a future date")
    @Schema(description = "Requested time for appointment", example = "2026-12-06T10:30:00Z")
    private Instant time;
}