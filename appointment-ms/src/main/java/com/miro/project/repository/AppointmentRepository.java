package com.miro.project.repository;

import com.miro.project.model.Appointment;
import com.miro.project.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findAllByPatientId(UUID patientId);
    List<Appointment> findAllByDoctorId(UUID doctorId);
    List<Appointment> findAllByStatus(AppointmentStatus status);
}