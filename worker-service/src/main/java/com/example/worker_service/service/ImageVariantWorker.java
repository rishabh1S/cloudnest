package com.example.worker_service.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.worker_service.model.dto.FileJob;
import com.example.worker_service.model.dto.FileUpdateRequest;
import com.example.worker_service.model.dto.FileVariantDto;
import com.example.worker_service.model.enums.FileStatus;
import com.example.worker_service.util.UpdateInternalUtils;
import com.example.worker_service.util.VariantUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageVariantWorker implements MessageListener {

    private final MinioStorageService minioStorageService;
    private final UpdateInternalUtils updateInternalUtils;
    private final ObjectMapper objectMapper;
    private final VariantUtils variantUtils;


    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            FileJob job = objectMapper.readValue(message.getBody(), FileJob.class);
            log.info("Received job for object: {}", job.getObjectKey());
            processJob(job);
        } catch (IOException e) {
            log.error("Failed to process Redis message: {}", e.getMessage(), e);
        }
    }

    private void processJob(FileJob job) {
        String status = FileStatus.COMPLETED.name();
        List<FileVariantDto> variants = new ArrayList<>();

        try (InputStream input = minioStorageService.getObject(job.getObjectKey())) {
            BufferedImage original = ImageIO.read(input);
            variants = variantUtils.generateImageVariants(job.getObjectKey(), original);
        } catch (Exception e) {
            status = FileStatus.FAILED.name();
            log.error("Error generating image variants for fileId {}: {}", job.getFileId(), e.getMessage(), e);
        } finally {
            FileUpdateRequest update = FileUpdateRequest.builder()
                    .fileId(job.getFileId())
                    .status(status)
                    .variants(variants)
                    .build();
            updateInternalUtils.updateInternal(job, update);
        }
    }
}
