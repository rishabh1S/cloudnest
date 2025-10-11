package com.example.file.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.file.exception.FileStorageException;
import com.example.file.exception.UserNotFoundException;
import com.example.file.model.FileMetadata;
import com.example.file.model.User;
import com.example.file.repository.FileMetadataRepository;
import com.example.file.repository.UserRepository;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
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

    public String uploadFile(MultipartFile file) {
        log.info("Uploading file: {}", file.getOriginalFilename());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
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

    public byte[] downloadFile(String filename) {
        log.info("Downloading file: {}", filename);
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(filename).build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new FileStorageException("File download failed", e);
        }
    }

    public List<String> listFiles() {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucket).build());
            List<String> files = new ArrayList<>();
            for (Result<Item> result : results) {
                files.add(result.get().objectName());
            }
            return files;
        } catch (Exception e) {
            throw new FileStorageException("Listing files failed", e);
        }
    }

    @Transactional
    public void deleteFile(String filename) {
        try {
            // 1: Delete from MinIO
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucket).object(filename).build());
            // 2: Delete from DB
            fileMetadataRepository.deleteByFilename(filename);
        } catch (Exception e) {
            throw new FileStorageException("Delete failed", e);
        }
    }
}
