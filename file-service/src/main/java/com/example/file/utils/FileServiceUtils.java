package com.example.file.utils;

import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class FileServiceUtils {

        private FileServiceUtils() {
        }

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
                        "video/mpeg");

        public static boolean isDocumentType(String mimeType) {
                if (mimeType == null)
                        return false;

                return switch (mimeType) {
                        // Word documents
                        case "application/msword",
                                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                                true;

                        // Excel spreadsheets
                        case "application/vnd.ms-excel",
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ->
                                true;

                        // PowerPoint presentations
                        case "application/vnd.ms-powerpoint",
                                        "application/vnd.openxmlformats-officedocument.presentationml.presentation" ->
                                true;

                        // Text files
                        case "text/plain" -> true;

                        default -> false;
                };
        }

}
