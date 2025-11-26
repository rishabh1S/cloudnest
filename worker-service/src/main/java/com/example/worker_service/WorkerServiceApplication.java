package com.example.worker_service;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WorkerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkerServiceApplication.class, args);
	}

	@Bean
    public ApplicationRunner keepAliveRunner() {
        return args -> Thread.currentThread().join();
    }

}
