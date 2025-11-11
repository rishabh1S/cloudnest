package com.example.worker_service.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.worker_service.model.dto.FileJob;
import com.example.worker_service.model.dto.FileUpdateRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateInternalUtils {
    private final RestTemplate restTemplate;

    @Value("${file.service.url}")
    private String fileServiceUrl;

    public void updateInternal(FileJob job, FileUpdateRequest update) {
        try {
            restTemplate.postForObject(fileServiceUrl + "/internal/update", update, Void.class);
        } catch (Exception ex) {
            log.error("Failed to update FileService for PDF fileId {}: {}", job.getFileId(), ex.getMessage(), ex);
        }
    }
}
