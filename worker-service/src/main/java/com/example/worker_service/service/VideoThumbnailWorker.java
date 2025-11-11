package com.example.worker_service.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
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
public class VideoThumbnailWorker implements MessageListener {

    private final VariantUtils variantUtils;
    private final MinioStorageService minioStorageService;
    private final UpdateInternalUtils updateInternalUtils;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            FileJob job = objectMapper.readValue(message.getBody(), FileJob.class);
            log.info("Received video job for object: {}", job.getObjectKey());
            processVideo(job);
        } catch (Exception e) {
            log.error("Failed to process video message: {}", e.getMessage(), e);
        }
    }

    private void processVideo(FileJob job) {
        String status = FileStatus.COMPLETED.name();
        List<FileVariantDto> variants = new ArrayList<>();

        try (InputStream videoStream = minioStorageService.getObject(job.getObjectKey())) {
            File tempVideo = File.createTempFile("video-", ".mp4");
            Files.copy(videoStream, tempVideo.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            File frame = File.createTempFile("frame-", ".png");
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-y",
                    "-i", tempVideo.getAbsolutePath(),
                    "-ss", "00:00:01",
                    "-vframes", "1",
                    frame.getAbsolutePath());
            pb.redirectErrorStream(true);
            pb.start().waitFor();

            BufferedImage thumbnail = ImageIO.read(frame);
            variants = variantUtils.generateVideoThumbnailVariants(job.getObjectKey(), thumbnail);

        } catch (Exception e) {
            status = FileStatus.FAILED.name();
            log.error("Error generating video thumbnail for fileId {}: {}", job.getFileId(), e.getMessage(), e);
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
