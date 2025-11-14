package com.example.file.model.dto;

import java.time.Instant;
import java.util.UUID;

public record ShareLinkDto(
        UUID id,
        String url,
        Instant expiresAt,
        boolean hasPassword
) {}
