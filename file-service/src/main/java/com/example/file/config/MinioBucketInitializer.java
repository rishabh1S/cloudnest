package com.example.file.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.file.exception.BucketInitializationException;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioBucketInitializer {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @PostConstruct
    public void createBucketIfNotExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Bucket created: {}", bucket);
            } else {
                log.info("Bucket already exists: {}", bucket);
            }
        } catch (Exception e) {
            throw new BucketInitializationException("Failed to create MinIO bucket", e);
        }
    }
}