package com.example.link.model.dto;

import java.time.Instant;
import java.util.UUID;

public record LinkRequest(
    UUID fileId,
    String password,  
    Instant expiresAt 
) {}
