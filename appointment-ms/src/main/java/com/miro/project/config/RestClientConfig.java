package com.miro.project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${app.services.auth-url}")
    private String authServiceUrl;

    @Bean
    public RestClient authWebClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(2000);
        requestFactory.setReadTimeout(5000);

        return RestClient.builder()
                .baseUrl(authServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }
}