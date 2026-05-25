package com.miro.project.config;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

@Configuration
public class GoogleCalendarConfig {

    @Value("${app.google.calendar.credentials-path}")
    private String credentialsPath;

    @Value("${app.google.calendar.application-name}")
    private String applicationName;

    private final ResourceLoader resourceLoader;

    public GoogleCalendarConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public Calendar googleCalendarClient() throws Exception {

        // 1. Load the credentials from the configured path
        InputStream credentialsStream;
        try {
            credentialsStream = resourceLoader.getResource(credentialsPath).getInputStream();
        } catch (IOException e) {
            throw new RuntimeException("Resource not found: " + credentialsPath, e);
        }

        // 2. Generate the credentials and request the specific scope (Free/Busy requires calendar.readonly or calendar)
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR_READONLY));

        // 3. Build the Calendar client. The HttpCredentialsAdapter will automatically
        //    fetch and refresh the access token for you behind the scenes.
        return new Calendar.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        )
                .setApplicationName(applicationName)
                .build();
    }
}