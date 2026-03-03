// SalonBackendApplication.java
package com.saloon.saloon_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Add this annotation
public class SalonBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalonBackendApplication.class, args);
	}
}