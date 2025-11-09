package com.example.worker_service.model.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageJob {

    private final UUID fileId;
    private final String objectKey;
    private final String mimeType;

    @JsonCreator
    public ImageJob(
        @JsonProperty("fileId") UUID fileId,
        @JsonProperty("objectKey") String objectKey,
        @JsonProperty("mimeType") String mimeType) {
        this.fileId = fileId;
        this.objectKey = objectKey;
        this.mimeType = mimeType;
    }
}