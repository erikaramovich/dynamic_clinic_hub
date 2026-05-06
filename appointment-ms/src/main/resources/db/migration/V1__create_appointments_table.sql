CREATE TABLE appointments
(
    id               UUID PRIMARY KEY,
    patient_id       UUID                     NOT NULL, -- Logical FK to auth-ms Users(id) where role='PATIENT'
    doctor_id        UUID,                              -- Logical FK to auth-ms Users(id) where role='DOCTOR'
    appointment_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status           VARCHAR(20)              NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Constraint to ensure status is valid
    CONSTRAINT chk_status CHECK (status IN ('requested', 'booked', 'assigned', 'cancelled', 'completed'))
);

CREATE INDEX idx_appointments_patient ON appointments (patient_id);
CREATE INDEX idx_appointments_doctor ON appointments (doctor_id);