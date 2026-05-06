package com.miro.project.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.util.Collections;

@Configuration
public class GoogleCalendarConfig {

    @Bean
    public Calendar googleCalendarClient() throws Exception {

        // 1. Load the credentials from the resources folder
        InputStream credentialsStream = getClass().getResourceAsStream("/google-credentials.json");
        if (credentialsStream == null) {
            throw new RuntimeException("Resource not found: google-credentials.json");
        }

        // 2. Generate the credentials and request the specific scope (Free/Busy requires calendar.readonly or calendar)
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR_READONLY));

        // 3. Build the Calendar client. The HttpCredentialsAdapter will automatically
        //    fetch and refresh the access token for you behind the scenes.
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        )
                .setApplicationName("appointment-ms")
                .build();
    }
}