package com.example.file.model.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileResponseDto {
    private UUID id;
    private String name;
    private String type;
    private long size;
    private Instant createdAt;
    private String previewUrl;
}
