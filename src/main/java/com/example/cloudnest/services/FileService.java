package com.example.cloudnest.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.cloudnest.model.entity.FileMetadata;
import com.example.cloudnest.model.entity.User;
import com.example.cloudnest.repository.FileMetadataRepository;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {
    private final MinioClient minioClient;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${minio.bucket}")
    private String bucket;
    // Temporary default user until auth is added
    private final User defaultUser = new User(UUID.randomUUID(), "default", "default@example.com", "password");

    public String uploadFile(MultipartFile file) {
        try {
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
            metadata.setOwner(defaultUser); // temporary
            fileMetadataRepository.save(metadata);

            return filename;
        } catch (Exception e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

    public byte[] downloadFile(String filename) {
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

    public void deleteFile(String filename) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucket).object(filename).build());
            fileMetadataRepository.deleteByFilename(filename);
        } catch (Exception e) {
            throw new RuntimeException("Delete failed", e);
        }
    }
}
