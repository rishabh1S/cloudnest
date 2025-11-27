package com.example.file.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class HealthCheck {

    @GetMapping("/health")
    public String healthCheck() {
        return "File Server is running successfully.";
    }
}
