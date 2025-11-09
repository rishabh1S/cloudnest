package com.example.worker_service.model.dto;

import java.util.UUID;

import com.example.worker_service.model.enums.JobType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileJob {
    private UUID fileId;
    private String objectKey;
    private String mimeType;
    private JobType jobType;
}