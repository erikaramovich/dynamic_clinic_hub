package com.miro.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Defines how passwords are encrypted in the database
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configures HTTP Security
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF as we use JWTs (stateless)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Allow anyone to register/login
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll() // Allow swagger
                        // Add this line to allow access to metrics and health checks
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated() // Protect everything else
                );
        return http.build();
    }
}