package com.miro.project.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentEvent {
    private UUID appointmentId;
    private UUID patientId;
    private UUID doctorId;
    private Instant appointmentTime;
    private AppointmentStatus status;
    private Instant eventTimestamp;
}