package com.miro.project.service;

import com.miro.project.dto.request.AppointmentRequest;
import com.miro.project.dto.response.UserInternalResponse;
import com.miro.project.model.Appointment;
import com.miro.project.model.AppointmentEvent;
import com.miro.project.model.AppointmentStatus;
import com.miro.project.repository.AppointmentRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

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
    private final RestClient authWebClient; // Injected from RestClientConfig

    // API 1: Fetch list of doctors from auth-ms for the UI
    public List<UserInternalResponse> getAvailableDoctors() {
        return authWebClient.get()
                .uri("/api/internal/users/doctors")
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {
                });
    }

    public List<Instant> getAvailableSlots(String doctorName, Instant dayStart, Instant dayEnd) {
        UserInternalResponse doctor = resolveDoctorByName(doctorName);
        return calendarService.getAvailableSlots(doctor.getEmail(), dayStart, dayEnd);
    }

    @Transactional
    public Appointment createAppointment(AppointmentRequest request, UUID patientId) {
        UUID doctorId = null;
        AppointmentStatus status = AppointmentStatus.REQUESTED;

        // If a name was provided, resolve it to UUID and Email via auth-ms
        if (request.getDoctorName() != null && !request.getDoctorName().isBlank()) {
            UserInternalResponse doctor = resolveDoctorByName(request.getDoctorName());

            // Validate availability in Google Calendar using resolved email
            if (!calendarService.isSlotAvailable(doctor.getEmail(), request.getTime())) {
                throw new RuntimeException("Doctor " + request.getDoctorName() + " is busy at this time.");
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

        Counter.builder("appointment_created_total")
                .description("Total number of appointments created")
                .tag("status", appointment.getStatus().name())
                .register(meterRegistry)
                .increment();

        publishEvent(appointment); // EXACTLY_ONCE triggered here
        return appointment;
    }

    @Transactional
    public void assignDoctor(UUID appointmentId, String doctorName) {
        Appointment app = repository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        UserInternalResponse doctor = resolveDoctorByName(doctorName);

        app.setDoctorId(doctor.getId());
        app.setStatus(AppointmentStatus.ASSIGNED);
        repository.save(app);
        publishEvent(app);
    }

    @Transactional
    public void cancelAppointment(UUID id, UUID requesterId, String role) {
        Appointment app = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        // IDOR Check
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

    // INTERNAL HELPER: Uses RestClient to resolve Name -> ID/Email
    private UserInternalResponse resolveDoctorByName(String name) {
        return authWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/internal/users/search")
                        .queryParam("name", name)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new RuntimeException("Doctor with name '" + name + "' not found in clinic records.");
                })
                .body(UserInternalResponse.class);
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

    public Page<Appointment> getAppointmentsByStatus(AppointmentStatus status, Pageable pageable) {
        return repository.findAllByStatus(status, pageable);
    }

    public Page<Appointment> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

}