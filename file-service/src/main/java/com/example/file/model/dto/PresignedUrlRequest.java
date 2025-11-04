package com.example.file.model.dto;

public record PresignedUrlRequest(String filename, String contentType,Long size) {
}
