package com.example.file.model.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileVariantDto {
    private String variantKey;   // e.g. "thumbnail", "medium", "original"
    private String url;
    private Map<String, Object> transform; // { "w":150,"h":150,"fmt":"webp" }
    private Long sizeBytes;
}
