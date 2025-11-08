package com.example.file.model.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record FileResponseDto(
    UUID id,
    String name,
    String type,
    long size,
    Instant createdAt,
    Map<String, String> variants
) {}
