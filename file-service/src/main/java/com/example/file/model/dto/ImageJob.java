package com.example.file.model.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageJob {
    private UUID fileId;
    private String objectKey;
    private String mimeType;
}