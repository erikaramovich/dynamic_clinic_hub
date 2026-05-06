package com.miro.project.repository;

import com.miro.project.model.Appointment;
import com.miro.project.model.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    Page<Appointment> findAllByPatientId(UUID patientId, Pageable pageable);
    Page<Appointment> findAllByStatus(AppointmentStatus status, Pageable pageable);
    Page<Appointment> findAll(Pageable pageable);
    Page<Appointment> findAllByDoctorId(UUID id, Pageable pageable);
}