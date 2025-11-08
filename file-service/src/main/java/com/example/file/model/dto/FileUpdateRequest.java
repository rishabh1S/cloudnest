package com.example.file.model.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUpdateRequest {
    private UUID fileId;
    private String status;
    private List<FileVariantDto> variants;
}

