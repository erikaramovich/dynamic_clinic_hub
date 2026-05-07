package com.miro.project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${app.services.auth-url}")
    private String authServiceUrl;

    @Bean
    public RestClient authWebClient() {
        return RestClient.builder()
                .baseUrl(authServiceUrl)
                .build();
    }
}