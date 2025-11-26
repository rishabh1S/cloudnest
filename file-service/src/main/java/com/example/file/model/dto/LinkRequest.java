package com.example.file.model.dto;

import java.time.Instant;
import java.util.UUID;

public record LinkRequest(
    UUID fileId,
    String password,  
    Instant expiresAt 
) {}
