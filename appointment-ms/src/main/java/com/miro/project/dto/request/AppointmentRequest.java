package com.miro.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class AppointmentRequest {
    @Schema(description = "Your preferred Doctor Id or nothing", example = "Dr.John")
    private UUID doctorId; // Can be null for 'requested' status

    @Schema(description = "Requested time for appointment", example = "2026-05-06T04:30:00")
    private Instant time;
}