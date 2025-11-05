package com.example.file.model.dto;

import java.time.Instant;
import java.util.UUID;

public record FileResponseDto(
    UUID id,
    String name,
    String type,
    long size,
    Instant createdAt,
    String thumbnailUrl
) {}
