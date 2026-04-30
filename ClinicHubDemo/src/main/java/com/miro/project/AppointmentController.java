package com.miro.project;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointments")
@Tag(name = "Appointment Service")
public class AppointmentController {

    @GetMapping
    public String getAppointments() {
        return null;
    }

    @PostMapping
    public String bookAppointment() {
        return null;
    }

    @PatchMapping("/{appointmentId}/status")
    public String updateAppointmentStatus(@PathVariable Long appointmentId) {
        return null;
    }
}