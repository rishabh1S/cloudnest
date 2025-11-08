package com.example.worker_service.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileStatus {
    UPLOADED("Uploaded"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed");

    private final String label;
}
