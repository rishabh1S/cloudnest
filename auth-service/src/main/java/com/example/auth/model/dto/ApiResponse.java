package com.example.auth.model.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse {
    private String message;
    private int statusCode;
    @Builder.Default
    private Instant time = Instant.now();
}