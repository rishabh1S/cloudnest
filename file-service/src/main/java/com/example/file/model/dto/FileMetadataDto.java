package com.example.file.model.dto;

import java.util.UUID;

public record FileMetadataDto(
    UUID id,
    String filename,
    String contentType,
    String url
) {}
