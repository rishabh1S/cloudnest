package com.example.file.constant;

import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class FileConstant {
    private FileConstant() {}

    public static final Set<String> ALLOWED_TYPES = Set.of(
            // Images
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/svg+xml",
            // Documents
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "application/zip",
            "application/x-zip-compressed",
            // Audio
            "audio/mpeg",
            "audio/wav", 
            "audio/ogg", 
            "audio/aac", 
            "audio/flac", 
            "audio/webm", 
            // Video
            "video/mp4", 
            "video/x-msvideo", 
            "video/x-matroska", 
            "video/webm", 
            "video/quicktime", 
            "video/mpeg" 
    );
}
