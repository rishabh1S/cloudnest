package com.example.file.service;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.file.exception.FileStorageException;
import com.example.file.exception.UserNotFoundException;
import com.example.file.model.dto.FileResponseDto;
import com.example.file.model.entity.FileMetadata;
import com.example.file.model.entity.User;
import com.example.file.repository.FileMetadataRepository;
import com.example.file.repository.UserRepository;
import com.example.file.utils.JsonUtils;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
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

    @Value("${minio.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file, String userHeader) {
        log.info("Uploading file: {}", file.getOriginalFilename());
        User user = JsonUtils.fromJson(userHeader, User.class);
        log.info("Request from user: {}", user.getEmail());
        String email = user.getEmail();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String filename = file.getOriginalFilename();
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(filename)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
        } catch (Exception e) {
            throw new FileStorageException("File upload failed", e);
        }
        FileMetadata metadata = new FileMetadata();
        metadata.setFilename(filename);
        metadata.setSize(file.getSize());
        metadata.setUrl(bucket + "/" + filename); // simple reference
        metadata.setOwner(owner);
        fileMetadataRepository.save(metadata);

        return filename;
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

    public List<FileResponseDto> listFiles(String userHeader) {
        try {
            User user = JsonUtils.fromJson(userHeader, User.class);
            log.info("Listing files for user: {}", user.getEmail());

            User owner = userRepository.findByEmail(user.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            List<FileMetadata> fileEntities = fileMetadataRepository.findByOwner(owner);

            return fileEntities.stream().map(f -> {
                String originalName = f.getFilename();
                String type = "";
                int idx = originalName.lastIndexOf('.');
                if (idx != -1 && idx < originalName.length() - 1) {
                    type = originalName.substring(idx + 1);
                }

                return new FileResponseDto(f.getId(),
                        originalName,
                        type,
                        f.getSize(),
                        f.getCreatedAt(),
                        f.getUrl() 
                );
            }).toList();

        } catch (Exception e) {
            throw new FileStorageException("Listing files failed", e);
        }
    }

    @Transactional
    public void deleteFile(UUID fileId, String userHeader) {
        try {
            User user = JsonUtils.fromJson(userHeader, User.class);
            FileMetadata file = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new FileStorageException("File not found", new RuntimeException()));

            // Check if user owns the file
            if (!file.getOwner().getEmail().equals(user.getEmail())) {
                throw new FileStorageException("Not authorized to delete this file", new RuntimeException());
            }

            // 1: Delete from MinIO
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(file.getFilename())
                    .build());

            // 2: Delete from DB
            fileMetadataRepository.deleteById(fileId);
        } catch (Exception e) {
            throw new FileStorageException("Delete failed", e);
        }
    }
}
