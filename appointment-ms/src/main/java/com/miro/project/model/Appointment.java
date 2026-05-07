package com.miro.project.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // REQUIRED: For auditing annotations
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "doctor_id")
    private UUID doctorId;

    @Column(name = "appointment_time", nullable = false)
    private Instant appointmentTime;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @CreatedDate // Automatically sets when saved to DB
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate // Automatically updates on any change
    @Column(name = "updated_at")
    private Instant updatedAt;
}