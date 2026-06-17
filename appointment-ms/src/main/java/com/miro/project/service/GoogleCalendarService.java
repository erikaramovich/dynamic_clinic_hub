package com.miro.project.service;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.FreeBusyRequest;
import com.google.api.services.calendar.model.FreeBusyRequestItem;
import com.google.api.services.calendar.model.FreeBusyResponse;
import com.google.api.services.calendar.model.TimePeriod;
import com.miro.project.exception.GoogleApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleCalendarService {
    private final Calendar calendarClient;
    private final GoogleCalendarService self;

    public GoogleCalendarService(Calendar calendarClient, @Lazy GoogleCalendarService self) {
        this.calendarClient = calendarClient;
        this.self = self;
    }

    @Value("${app.business.hours.start-utc}")
    private int businessStartHour;

    @Value("${app.business.hours.end-utc}")
    private int businessEndHour;

    @Value("${app.business.slot-duration-minutes}")
    private int slotDuration;

    @Cacheable(value = "slots")
    @CircuitBreaker(name = "googleCalendar")
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
            throw new GoogleApiException("Google Calendar API failure: " + e.getMessage());
        }
    }

    public boolean isSlotAvailable(String doctorEmail, Instant requestedTime) {
        // Business hours check for direct booking
        ZonedDateTime zdt = requestedTime.atZone(ZoneOffset.UTC);
        int hour = zdt.getHour();
        if (hour < businessStartHour || hour >= businessEndHour) {
            return false;
        }

        List<Instant> availableSlots = self.getAvailableSlots(doctorEmail,
                requestedTime.minus(1, ChronoUnit.MINUTES),
                requestedTime.plus(slotDuration + 1, ChronoUnit.MINUTES));
        return !availableSlots.isEmpty();
    }

    private List<Instant> calculateGaps(Instant start, Instant end, List<TimePeriod> busy) {
        List<Instant> slots = new ArrayList<>();
        Instant current = start;

        while (current.plus(slotDuration, ChronoUnit.MINUTES).isBefore(end)) {
            ZonedDateTime zdt = current.atZone(ZoneOffset.UTC);
            int hour = zdt.getHour();

            if (hour >= businessStartHour && hour < businessEndHour) {
                Instant finalCurrent = current;
                boolean isBusy = busy.stream().anyMatch(p ->
                        finalCurrent.isBefore(Instant.ofEpochMilli(p.getEnd().getValue())) &&
                                finalCurrent.plus(slotDuration, ChronoUnit.MINUTES).isAfter(Instant.ofEpochMilli(p.getStart().getValue()))
                );
                if (!isBusy) slots.add(current);
            }
            current = current.plus(slotDuration, ChronoUnit.MINUTES);
        }
        return slots;
    }
}