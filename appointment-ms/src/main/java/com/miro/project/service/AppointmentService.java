package com.miro.project.service;

import com.miro.project.dto.request.AppointmentRequest;
import com.miro.project.dto.response.UserInternalResponse;
import com.miro.project.exception.SlotUnavailableException;
import com.miro.project.model.Appointment;
import com.miro.project.model.AppointmentEvent;
import com.miro.project.model.AppointmentStatus;
import com.miro.project.repository.AppointmentRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.miro.project.config.KafkaConfig.TOPIC_NAME;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository repository;
    private final MeterRegistry meterRegistry;
    private final GoogleCalendarService calendarService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final DoctorServiceClient doctorServiceClient;

    public List<UserInternalResponse> getAvailableDoctors() {
        return doctorServiceClient.getAvailableDoctors();
    }

    public List<Instant> getAvailableSlots(String doctorName, Instant dayStart, Instant dayEnd) {
        UserInternalResponse doctor = doctorServiceClient.resolveDoctorByName(doctorName);
        return calendarService.getAvailableSlots(doctor.getEmail(), dayStart, dayEnd);
    }

    @Transactional
    public Appointment createAppointment(AppointmentRequest request, UUID patientId) {
        AppointmentStatus status = AppointmentStatus.REQUESTED;
        String doctorName = request.getDoctorName();
        UUID doctorId = null;

        if (doctorName != null && !doctorName.isBlank()) {
            UserInternalResponse doctor = doctorServiceClient.resolveDoctorByName(doctorName);

            if (!calendarService.isSlotAvailable(doctor.getEmail(), request.getTime())) {
                throw new SlotUnavailableException("Doctor " + doctorName + " is busy at this time.");
            }
            doctorId = doctor.getId();
            status = AppointmentStatus.BOOKED;
        }

        Appointment appointment = Appointment.builder()
                .patientId(patientId)
                .doctorId(doctorId)
                .appointmentTime(request.getTime())
                .status(status)
                .build();

        appointment = repository.save(appointment);

        incrementCreatedCounter(appointment.getStatus());
        publishEvent(appointment);
        return appointment;
    }

    @Transactional
    public void assignDoctor(UUID appointmentId, String doctorName) {
        Appointment app = repository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        UserInternalResponse doctor = doctorServiceClient.resolveDoctorByName(doctorName);

        app.setDoctorId(doctor.getId());
        app.setStatus(AppointmentStatus.ASSIGNED);
        repository.save(app);
        publishEvent(app);
    }

    @Transactional
    public void cancelAppointment(UUID id, UUID requesterId, String role) {
        Appointment app = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (role.equals("ROLE_PATIENT") && !app.getPatientId().equals(requesterId)) {
            throw new RuntimeException("Forbidden: Not your appointment");
        }
        app.setStatus(AppointmentStatus.CANCELLED);
        repository.save(app);
        publishEvent(app);
    }

    @Transactional
    public void updateStatus(UUID id, AppointmentStatus status) {
        Appointment app = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        app.setStatus(status);
        repository.save(app);
        publishEvent(app);
    }

    private void incrementCreatedCounter(AppointmentStatus status) {
        Counter.builder("appointment_created_total")
                .description("Total number of appointments created")
                .tag("status", status.name())
                .register(meterRegistry)
                .increment();
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

        kafkaTemplate.send(TOPIC_NAME, app.getId().toString(), event);
    }

    public Page<Appointment> getPatientAppointments(UUID id, Pageable pageable) {
        return repository.findAllByPatientId(id, pageable);
    }

    public Page<Appointment> getDoctorAppointments(UUID id, Pageable pageable) {
        return repository.findAllByDoctorId(id, pageable);
    }

    public Page<Appointment> getAppointmentsByStatus(AppointmentStatus status, Pageable pageable) {
        return repository.findAllByStatus(status, pageable);
    }

    public Page<Appointment> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

}