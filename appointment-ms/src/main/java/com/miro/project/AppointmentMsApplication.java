package com.miro.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // REQUIRED: Enables automated auditing like @CreatedDate
public class AppointmentMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppointmentMsApplication.class, args);
	}

}