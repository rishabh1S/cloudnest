package com.example.worker_service.service;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.worker_service.dto.FileUpdateRequest;
import com.example.worker_service.dto.FileVariantDto;
import com.example.worker_service.dto.ImageJob;
import com.example.worker_service.enums.FileStatus;
import com.example.worker_service.exception.ImageProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageVariantWorker implements MessageListener {

    private final MinioStorageService minioStorageService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${file.service.url}")
    private String fileServiceUrl;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody());
            ImageJob job = objectMapper.readValue(json, ImageJob.class);
            log.info("Received job for object: {}", job.getObjectKey());
            processJob(job);
        } catch (Exception e) {
            log.error("Failed to process Redis message: {}", e.getMessage(), e);
        }
    }

    private void processJob(ImageJob job) {
        List<FileVariantDto> variants = new ArrayList<>();
        String status = FileStatus.COMPLETED.name();

        try (InputStream input = minioStorageService.getObject(job.getObjectKey())) {
            BufferedImage original = ImageIO.read(input);
            if (original == null)
                throw new ImageProcessingException("Cannot read image: " + job.getObjectKey());

            // Variant definitions
            Map<String, Integer> variantSizes = Map.of(
                    "medium", 800,
                    "thumbnail", 200);

            // Generate variants
            for (Map.Entry<String, Integer> entry : variantSizes.entrySet()) {
                String variantKey = entry.getKey();
                int size = entry.getValue();
                minioStorageService.uploadVariant(original, job.getObjectKey(), variantKey, size);
                variants.add(buildVariantDto(job.getObjectKey(), variantKey));
            }

            // Add original variant
            variants.add(buildVariantDto(job.getObjectKey(), "original"));

        } catch (Exception e) {
            status = FileStatus.FAILED.name();
            log.error("Error processing image job for fileId {}: {}", job.getFileId(), e.getMessage(), e);
        } finally {
            // Update FileService
            FileUpdateRequest update = FileUpdateRequest.builder()
                    .fileId(job.getFileId())
                    .status(status)
                    .variants(variants)
                    .build();

            try {
                restTemplate.postForObject(fileServiceUrl + "/internal/update", update, Void.class);
                log.info("File status updated to {} for fileId: {}", status, job.getFileId());
            } catch (RestClientException e) {
                log.error("Failed to notify FileService for fileId {}: {}", job.getFileId(), e.getMessage(), e);
            }
        }
    }

    private FileVariantDto buildVariantDto(String objectKey, String variantKey) {
        long size = minioStorageService.getSize(objectKey, variantKey);
        String url = minioStorageService.buildUrl(objectKey, variantKey);

        return FileVariantDto.builder()
                .variantKey(variantKey)
                .url(url)
                .transform(variantKey.equals("original") ? Map.of() : Map.of("w", getWidthForVariant(variantKey)))
                .sizeBytes(size)
                .build();
    }

    private int getWidthForVariant(String variantKey) {
        return switch (variantKey) {
            case "medium" -> 800;
            case "thumbnail" -> 200;
            default -> 0;
        };
    }
}
