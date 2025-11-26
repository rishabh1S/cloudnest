package com.example.file.model.dto;

import java.time.Instant;

public record LinkResponse(
    String token,
    String url,
    Instant expiresAt
) {}
