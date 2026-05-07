package com.miro.project.dto.response;

import com.miro.project.model.AppointmentStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AppointmentResponse {
    private UUID id;
    private UUID patientId;
    private UUID doctorId;
    private Instant appointmentTime;
    private AppointmentStatus status;
}