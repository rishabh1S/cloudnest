package com.example.file.service;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.file.constant.FileConstant;
import com.example.file.exception.FileStorageException;
import com.example.file.exception.UserNotFoundException;
import com.example.file.model.dto.FileResponseDto;
import com.example.file.model.dto.PresignedUrlResponse;
import com.example.file.model.entity.FileMetadata;
import com.example.file.model.entity.User;
import com.example.file.repository.FileMetadataRepository;
import com.example.file.repository.UserRepository;
import com.example.file.utils.JsonUtils;

import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    private final MinioClient minioClient;
    private final FileMetadataRepository fileMetadataRepository;
    private final UserRepository userRepository;
    private final ThumbnailService thumbnailService;
    private static final Set<String> ALLOWED_TYPES = FileConstant.ALLOWED_TYPES;

    @Value("${minio.bucket}")
    private String bucket;

    public PresignedUrlResponse generatePresignedUrl(String filename, String contentType, Long size,
            String userHeader) {
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new FileStorageException("File type not allowed", new RuntimeException());
        }

        User user = JsonUtils.fromJson(userHeader, User.class);
        User owner = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String objectKey = owner.getId() + "/" + UUID.randomUUID() + "_" + filename;

        try {
            String uploadUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(10, TimeUnit.MINUTES)
                            .build());

            FileMetadata meta = new FileMetadata();
            meta.setFilename(filename);
            meta.setObjectKey(objectKey);
            meta.setSize(size);
            meta.setMimeType(contentType);
            meta.setFileStatus("UPLOADING");
            meta.setOwner(owner);
            meta.setUrl("http://minio:9000/" + bucket + "/" + objectKey);
            meta.setCreatedAt(Instant.now());
            fileMetadataRepository.save(meta);

            return new PresignedUrlResponse(objectKey, uploadUrl);
        } catch (Exception e) {
            throw new FileStorageException("Failed to generate presigned URL", e);
        }
    }

    @Transactional
    public FileResponseDto completeUpload(String objectKey, String userHeader) {
        User user = JsonUtils.fromJson(userHeader, User.class);
        User owner = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        FileMetadata meta = fileMetadataRepository.findByObjectKey(objectKey)
                .orElseThrow(() -> new FileStorageException("File not found", new RuntimeException()));

        if (!meta.getOwner().getId().equals(owner.getId())) {
            throw new FileStorageException("Unauthorized upload confirmation", new RuntimeException());
        }

        meta.setFileStatus("READY");
        fileMetadataRepository.save(meta);

        if (meta.getMimeType().startsWith("image/")) {
            thumbnailService.generateThumbnailAsync(meta);
        }

        return new FileResponseDto(meta.getId(), meta.getFilename(),
                meta.getMimeType(), meta.getSize(), meta.getCreatedAt(), meta.getThumbnailUrl());
    }

    public List<FileResponseDto> listFiles(String userHeader) {
        User user = JsonUtils.fromJson(userHeader, User.class);
        User owner = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return fileMetadataRepository.findByOwner(owner)
                .stream()
                .map(f -> new FileResponseDto(f.getId(), f.getFilename(), f.getMimeType(),
                        f.getSize(), f.getCreatedAt(), f.getThumbnailUrl()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFile(UUID fileId, String userHeader) {
        User user = JsonUtils.fromJson(userHeader, User.class);
        FileMetadata file = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new FileStorageException("File not found", new RuntimeException()));

        if (!file.getOwner().getEmail().equals(user.getEmail())) {
            throw new FileStorageException("Not authorized to delete this file", new RuntimeException());
        }

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(file.getObjectKey())
                    .build());

            fileMetadataRepository.delete(file);
        } catch (Exception e) {
            throw new FileStorageException("Delete failed", e);
        }
    }

    public byte[] downloadFile(UUID fileId) {
        log.info("Downloading file with ID: {}", fileId);
        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new FileStorageException("File not found", new RuntimeException()));

        String filename = fileMetadata.getFilename();
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(filename).build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new FileStorageException("File download failed", e);
        }
    }
}
