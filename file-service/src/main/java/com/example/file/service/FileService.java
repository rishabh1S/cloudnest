package com.example.file.service;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.file.exception.FileStorageException;
import com.example.file.exception.UserNotFoundException;
import com.example.file.model.dto.FileJob;
import com.example.file.model.dto.FileResponseDto;
import com.example.file.model.dto.FileUpdateRequest;
import com.example.file.model.dto.PresignedUrlResponse;
import com.example.file.model.entity.FileMetadata;
import com.example.file.model.entity.FileVariant;
import com.example.file.model.entity.User;
import com.example.file.model.enums.FileStatus;
import com.example.file.model.enums.JobType;
import com.example.file.repository.FileMetadataRepository;
import com.example.file.repository.FileVariantRepository;
import com.example.file.repository.UserRepository;
import com.example.file.utils.FileServiceUtils;
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
    private final FileVariantRepository fileVariantRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String USER_NOT_FOUND = "User not found";
    private static final String FILE_NOT_FOUND = "File not found";
    private static final String IMAGE_QUEUE = "image:variant:queue";
    private static final String VIDEO_QUEUE = "video:thumbnail:queue";
    private static final String PDF_QUEUE = "pdf:preview:queue";
    private static final String DOC_QUEUE = "doc:preview:queue";

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.url}")
    private String minioUrl;

    public PresignedUrlResponse generatePresignedUrl(String filename, String contentType, Long size,
            String userHeader) {
        if (!FileServiceUtils.ALLOWED_TYPES.contains(contentType)) {
            throw new FileStorageException("File type not allowed", new RuntimeException());
        }

        User user = JsonUtils.fromJson(userHeader, User.class);
        User owner = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

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
            meta.setFileStatus(FileStatus.UPLOADED.name());
            meta.setOwner(owner);
            meta.setUrl(minioUrl + "/" + bucket + "/" + objectKey);
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
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        FileMetadata meta = fileMetadataRepository.findByObjectKey(objectKey)
                .orElseThrow(() -> new FileStorageException(FILE_NOT_FOUND, new RuntimeException()));

        if (!meta.getOwner().getId().equals(owner.getId())) {
            throw new FileStorageException("Unauthorized upload confirmation", new RuntimeException());
        }

        meta.setFileStatus(FileStatus.PROCESSING.name());
        fileMetadataRepository.save(meta);

        Map<String, String> variants = Map.of("original", meta.getUrl());

        if (meta.getMimeType().startsWith("image/")) {
            sendJob(meta, JobType.IMAGE, IMAGE_QUEUE);
        } else if (meta.getMimeType().startsWith("video/")) {
            sendJob(meta, JobType.VIDEO, VIDEO_QUEUE);
        } else if (meta.getMimeType().equals("application/pdf")) {
            sendJob(meta, JobType.PDF, PDF_QUEUE);
        } else if (FileServiceUtils.isDocumentType(meta.getMimeType())) {
            sendJob(meta, JobType.DOCUMENT, DOC_QUEUE);
        } else {
            meta.setFileStatus(FileStatus.COMPLETED.name());
            fileMetadataRepository.save(meta);
        }

        return new FileResponseDto(meta.getId(), meta.getFilename(), meta.getMimeType(),
                meta.getSize(), meta.getCreatedAt(), variants);
    }

    private void sendJob(FileMetadata meta, JobType jobType, String queue) {
        FileJob job = new FileJob(meta.getId(), meta.getObjectKey(), meta.getMimeType(), jobType);
        redisTemplate.convertAndSend(queue, job);
    }

    @Transactional
    public void updateFileVariants(FileUpdateRequest updateRequest) {
        FileMetadata file = fileMetadataRepository.findById(updateRequest.getFileId())
                .orElseThrow(() -> new FileStorageException(FILE_NOT_FOUND, new RuntimeException()));

        // Clear old variants (in case of reprocessing)
        fileVariantRepository.deleteAll(fileVariantRepository.findByFile_Id(file.getId()));

        updateRequest.getVariants().forEach(v -> {
            FileVariant variant = new FileVariant();
            variant.setFile(file);
            variant.setVariantKey(v.getVariantKey());
            variant.setUrl(v.getUrl());
            variant.setTransform(v.getTransform());
            variant.setSizeBytes(v.getSizeBytes());
            fileVariantRepository.save(variant);
        });

        file.setFileStatus(updateRequest.getStatus());
        fileMetadataRepository.save(file);

        log.info("File variants updated for fileId: {}", updateRequest.getFileId());
    }

    public List<FileResponseDto> listFiles(String userHeader) {
        User user = JsonUtils.fromJson(userHeader, User.class);
        User owner = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        return fileMetadataRepository.findByOwner(owner)
                .stream()
                .map(f -> {
                    var variants = fileVariantRepository.findByFile_Id(f.getId());
                    return new FileResponseDto(
                            f.getId(),
                            f.getFilename(),
                            f.getMimeType(),
                            f.getSize(),
                            f.getCreatedAt(),
                            variants.stream()
                                    .collect(Collectors.toMap(FileVariant::getVariantKey, FileVariant::getUrl)));
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFile(UUID fileId, String userHeader) {
        User user = JsonUtils.fromJson(userHeader, User.class);
        FileMetadata file = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new FileStorageException(FILE_NOT_FOUND, new RuntimeException()));

        if (!file.getOwner().getEmail().equals(user.getEmail())) {
            throw new FileStorageException("Not authorized to delete this file", new RuntimeException());
        }

        try {
            // Delete from MinIO
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(file.getObjectKey())
                    .build());

            // Delete variants from MinIO
            fileVariantRepository.findByFile_Id(fileId).forEach(v -> {
                try {
                    String variantPath = v.getUrl().substring(v.getUrl().indexOf(bucket) + bucket.length() + 1);
                    minioClient.removeObject(RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(variantPath)
                            .build());
                } catch (Exception ex) {
                    log.warn("Failed to delete variant {} from MinIO", v.getVariantKey());
                }
            });

            fileMetadataRepository.delete(file);
        } catch (Exception e) {
            throw new FileStorageException("Delete failed", e);
        }
    }

    public byte[] downloadFile(UUID fileId) {
        log.info("Downloading file with ID: {}", fileId);
        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new FileStorageException(FILE_NOT_FOUND, new RuntimeException()));

        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(fileMetadata.getObjectKey()).build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new FileStorageException("File download failed", e);
        }
    }
}
