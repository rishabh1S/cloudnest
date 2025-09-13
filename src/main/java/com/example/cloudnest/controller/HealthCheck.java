package com.example.cloudnest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthCheck {

    @GetMapping
    public String healthCheck() {
        return "Server is running successfully.";
    }
}
