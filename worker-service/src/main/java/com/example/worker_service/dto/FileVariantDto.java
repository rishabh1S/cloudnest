package com.example.worker_service.dto;

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
    private String variantKey;  
    private String url;
    private Map<String, Object> transform; 
    private Long sizeBytes;
}