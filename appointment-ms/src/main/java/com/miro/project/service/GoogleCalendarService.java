package com.miro.project.service;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GoogleCalendarService {
    private final Calendar calendarClient;

    public List<Instant> getAvailableSlots(String doctorEmail, Instant dayStart, Instant dayEnd) {
        try {
            FreeBusyRequest request = new FreeBusyRequest()
                    .setTimeMin(new com.google.api.client.util.DateTime(dayStart.toEpochMilli()))
                    .setTimeMax(new com.google.api.client.util.DateTime(dayEnd.toEpochMilli()))
                    .setItems(Collections.singletonList(new FreeBusyRequestItem().setId(doctorEmail)));

            FreeBusyResponse fbResponse = calendarClient.freebusy().query(request).execute();
            List<TimePeriod> busyPeriods = fbResponse.getCalendars().get(doctorEmail).getBusy();

            return calculateGaps(dayStart, dayEnd, busyPeriods);
        } catch (Exception e) {
            throw new RuntimeException("Google Calendar API failure: " + e.getMessage());
        }
    }


    public boolean isSlotAvailable(String doctorEmail, Instant requestedTime) {
        List<Instant> availableSlots = getAvailableSlots(doctorEmail,
                requestedTime.minus(1, ChronoUnit.MINUTES),
                requestedTime.plus(31, ChronoUnit.MINUTES));
        return !availableSlots.isEmpty();
    }

    private List<Instant> calculateGaps(Instant start, Instant end, List<TimePeriod> busy) {
        List<Instant> slots = new ArrayList<>();
        Instant current = start;
        // Slide a 30-minute window and check against busy periods
        while (current.plus(30, ChronoUnit.MINUTES).isBefore(end)) {
            Instant finalCurrent = current;
            boolean isBusy = busy.stream().anyMatch(p ->
                    finalCurrent.isBefore(Instant.ofEpochMilli(p.getEnd().getValue())) &&
                            finalCurrent.plus(30, ChronoUnit.MINUTES).isAfter(Instant.ofEpochMilli(p.getStart().getValue()))
            );
            if (!isBusy) slots.add(current);
            current = current.plus(30, ChronoUnit.MINUTES);
        }
        return slots;
    }
}