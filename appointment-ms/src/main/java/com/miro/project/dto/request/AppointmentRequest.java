package com.miro.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class AppointmentRequest {
    @Schema(description = "Your preferred Doctor Id or nothing", example = "5e80d829-b7ff-4e1e-a6ef-08e0e5bab429")
    private UUID doctorId;  // Can be null for 'requested' status

    @NotNull(message = "Appointment time is required")
    @Future(message = "Appointment must be scheduled for a future date")
    @Schema(description = "Requested time for appointment", example = "2026-12-06T10:30:00Z")
    private Instant time;
}