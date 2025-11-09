package com.example.file.model.dto;

import java.util.UUID;

import com.example.file.model.enums.JobType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileJob {
    private UUID fileId;
    private String objectKey;
    private String mimeType;
    private JobType jobType;
}
