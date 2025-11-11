package com.example.file.utils;

import java.text.Normalizer;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class FileServiceUtils {

        private FileServiceUtils() {
        }

        private static final Pattern NON_ASCII = Pattern.compile("[^\\x00-\\x7F]");
        private static final Pattern INVALID_CHARS = Pattern.compile("[^a-zA-Z0-9._-]");

        public static String normalizeFilename(String filename) {
                if (filename == null)
                        return "unnamed";

                String normalized = Normalizer.normalize(filename, Normalizer.Form.NFD);
                normalized = NON_ASCII.matcher(normalized).replaceAll("");

                normalized = INVALID_CHARS.matcher(normalized).replaceAll("_");

                if (normalized.length() > 200) {
                        normalized = normalized.substring(normalized.length() - 200, normalized.length());
                }

                return normalized;
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
