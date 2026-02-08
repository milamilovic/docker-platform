package com.dockerplatform.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DockerPlatformBackendApplication {

	private static final Logger log = LoggerFactory.getLogger(DockerPlatformBackendApplication.class);

	public static void main(String[] args) {
		log.info("Application is starting...");
		log.info("Logs will be sent to Logstash");
		SpringApplication.run(DockerPlatformBackendApplication.class, args);
		log.warn("Application may not start yet... Please wait");
		log.info("Application started");
	}

}
