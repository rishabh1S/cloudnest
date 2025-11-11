package com.example.link.model.dto;

import java.time.Instant;

public record LinkResponse(
    String token,
    String url,
    Instant expiresAt
) {}
