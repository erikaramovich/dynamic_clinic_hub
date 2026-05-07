package com.miro.project.service;

import com.miro.project.dto.request.AppointmentRequest;
import com.miro.project.model.Appointment;
import com.miro.project.model.AppointmentStatus;
import com.miro.project.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository repository;
    private final GoogleCalendarService calendarService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Logic for suggesting slots based on doctor UUID
    public List<Instant> getAvailableSlotsForDoctor(UUID doctorId, Instant start, Instant end) {
        String doctorEmail = resolveEmailFromId(doctorId); // Bridge between internal UUID and External Email
        return calendarService.getAvailableSlots(doctorEmail, start, end);
    }

    @Transactional
    public void cancelAppointment(UUID appointmentId, UUID requesterId, String role) {
        Appointment app = repository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // FIXED: IDOR Protection
        // Patients can only cancel their OWN appointments
        if (role.equals("ROLE_PATIENT") && !app.getPatientId().equals(requesterId)) {
            throw new RuntimeException("Access Denied: You cannot cancel another user's appointment");
        }

        // Doctors can only cancel appointments assigned to THEM
        if (role.equals("ROLE_DOCTOR") && !requesterId.equals(app.getDoctorId())) {
            throw new RuntimeException("Access Denied: You cannot cancel an appointment not assigned to you");
        }

        // Admins can cancel anything (no check needed here)

        app.setStatus(AppointmentStatus.CANCELLED);
        repository.save(app);
        publishEvent(app); // FIXED: Structured Event
    }

    @Transactional // Ensures DB save and Kafka send succeed or fail together (EXACTLY_ONCE)
    public Appointment createAppointment(AppointmentRequest request, UUID patientId) {
        // 1. Availability Check: Only if a doctor was specified
        if (request.getDoctorId() != null) {
            String email = resolveEmailFromId(request.getDoctorId());
            if (!calendarService.isSlotAvailable(email, request.getTime())) {
                throw new RuntimeException("The selected time slot is already occupied.");
            }
        }

        AppointmentStatus status = (request.getDoctorId() == null)
                ? AppointmentStatus.REQUESTED
                : AppointmentStatus.BOOKED;

        Appointment appointment = Appointment.builder()
                .patientId(patientId)
                .doctorId(request.getDoctorId())
                .appointmentTime(request.getTime())
                .status(status)
                .build();

        appointment = repository.save(appointment);
        publishEvent(appointment); // FIXED: Structured Event

        return appointment;
    }

    @Transactional
    public void assignDoctor(UUID appointmentId, UUID doctorId) {
        Appointment app = repository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        app.setDoctorId(doctorId);
        app.setStatus(AppointmentStatus.ASSIGNED);
        repository.save(app);
        publishEvent(app);
    }

    @Transactional
    public void updateStatus(UUID id, AppointmentStatus newStatus) {
        Appointment app = repository.findById(id).orElseThrow();
        app.setStatus(newStatus);
        repository.save(app);
        kafkaTemplate.send("appointment-events", id.toString(), "Event: STATUS_CHANGED_TO_" + newStatus.name());
    }

    private void publishEvent(Appointment app) {
        AppointmentEvent event = AppointmentEvent.builder()
                .appointmentId(app.getId())
                .patientId(app.getPatientId())
                .doctorId(app.getDoctorId())
                .appointmentTime(app.getAppointmentTime())
                .status(app.getStatus())
                .eventTimestamp(Instant.now())
                .build();

        kafkaTemplate.send("appointment-events", app.getId().toString(), event);
    }

    public Page<Appointment> getPatientAppointments(UUID id, Pageable pageable) {
        return repository.findAllByPatientId(id, pageable);
    }

    public Page<Appointment> getAppointmentsByStatus(AppointmentStatus status, Pageable pageable) {
        return repository.findAllByStatus(status, pageable);
    }

    public Page<Appointment> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /**
     * MOCK RESOLVER: In a real system, this would call the auth-ms via RestClient
     * or look up a local cache to find the email associated with the UUID.
     */
    private String resolveEmailFromId(UUID doctorId) {
        return "dr.john@miro-clinic.com";
    }
}