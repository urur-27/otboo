package com.team3.otboo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.team3.otboo.config")
@EnableScheduling
public class OtbooApplication {

	public static void main(String[] args) {
		SpringApplication.run(OtbooApplication.class, args);
	}

}

