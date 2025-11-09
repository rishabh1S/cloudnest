package com.example.worker_service.service;

import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.worker_service.exception.StorageException;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.url}")
    private String minioUrl;

    /** Get object input stream from MinIO */
    public InputStream getObject(String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(objectKey).build());
        } catch (Exception e) {
            throw new StorageException("Failed to fetch object from MinIO: " + objectKey, e);
        }
    }

    public void uploadVariantStream(InputStream is, long size, String objectKey, String variantKey, String contentType) {
    try {
        String minioPath = String.format("variants/%s/%s", variantKey, objectKey);
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(minioPath)
                        .stream(is, size, -1)
                        .contentType(contentType)
                        .build());
        log.info("Uploaded {} variant for {}", variantKey, objectKey);
    } catch (Exception e) {
        throw new StorageException("Failed to upload variant stream for " + objectKey, e);
    }
}

    /** Get size of variant */
    public long getSize(String objectKey, String variantKey) {
        try {
            String minioPath = "original".equals(variantKey) ? objectKey : "variants/" + variantKey + "/" + objectKey;
            return minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(minioPath).build()).size();
        } catch (Exception e) {
            throw new StorageException("Failed to get size for " + objectKey + " variant: " + variantKey, e);
        }
    }

    /** Build public URL for variant */
    public String buildUrl(String objectKey, String variantKey) {
        if ("original".equals(variantKey)) {
            return String.format("%s/%s/%s", minioUrl, bucket, objectKey);
        }
        return String.format("%s/%s/variants/%s/%s", minioUrl, bucket, variantKey, objectKey);
    }
}
