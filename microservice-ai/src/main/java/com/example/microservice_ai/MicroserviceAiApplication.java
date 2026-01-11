package com.example.microservice_ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MicroserviceAiApplication {
	private static final Logger logger = LoggerFactory.getLogger(MicroserviceAiApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Microservice AI Application");
		SpringApplication.run(MicroserviceAiApplication.class, args);
		logger.info("Microservice AI Application started successfully");
	}
}
