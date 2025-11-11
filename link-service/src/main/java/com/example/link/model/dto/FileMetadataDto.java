package com.example.link.model.dto;

import java.util.UUID;

public record FileMetadataDto(
    UUID id,
    String filename,
    String contentType,
    String url
) {}
