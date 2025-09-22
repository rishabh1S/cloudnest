package com.example.cloudnest.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.cloudnest.model.entity.FileMetadata;
import com.example.cloudnest.model.entity.User;
import com.example.cloudnest.repository.FileMetadataRepository;
import com.example.cloudnest.repository.UserRepository;

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
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            User owner = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(filename)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            FileMetadata metadata = new FileMetadata();
            metadata.setFilename(filename);
            metadata.setSize(file.getSize());
            metadata.setUrl(bucket + "/" + filename); // simple reference
            metadata.setOwner(owner);
            fileMetadataRepository.save(metadata);

            return filename;
        } catch (Exception e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

    public byte[] downloadFile(String filename) {
        log.info("Downloading file: {}", filename);
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(filename).build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("File download failed", e);
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
            throw new RuntimeException("Listing files failed", e);
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
            throw new RuntimeException("Delete failed", e);
        }
    }
}
