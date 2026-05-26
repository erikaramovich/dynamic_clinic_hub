package com.miro.project.mapper;

import com.miro.project.dto.response.AppointmentResponse;
import com.miro.project.model.Appointment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    AppointmentResponse toResponse(Appointment appointment);
}
