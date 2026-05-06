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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional // Ensures DB and Kafka are in one EXACTLY_ONCE transaction
    public Appointment createAppointment(AppointmentRequest request, UUID patientId) {
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

        // Status-specific Kafka event
        kafkaTemplate.send("appointment-events", appointment.getId().toString(),
                "Status updated to: " + status.name());

        return appointment;
    }

    @Transactional
    public void assignDoctor(UUID appointmentId, UUID doctorId) {
        Appointment appointment = repository.findById(appointmentId).orElseThrow();
        appointment.setDoctorId(doctorId);
        appointment.setStatus(AppointmentStatus.ASSIGNED);
        repository.save(appointment);
        kafkaTemplate.send("appointment-events", appointmentId.toString(), "Status: assigned");
    }

    @Transactional
    public void updateStatus(UUID id, AppointmentStatus newStatus) {
        Appointment app = repository.findById(id).orElseThrow();
        app.setStatus(newStatus);
        repository.save(app);
        kafkaTemplate.send("appointment-events", id.toString(), "Status: " + newStatus.name());
    }


    public Page<Appointment> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }


    public Page<Appointment> getPatientAppointments(UUID id, Pageable pageable) {
        return repository.findAllByPatientId(id, pageable);
    }

    public Page<Appointment> getAppointmentsByStatus(AppointmentStatus status, Pageable pageable) {
        return repository.findAllByStatus(status, pageable);
    }

    public Page<Appointment> getDoctorAppointments(UUID id, Pageable pageable) {
        return repository.findAllByDoctorId(id, pageable);
    }
}