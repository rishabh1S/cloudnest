package com.example.worker_service.service;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

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
public class PdfPreviewWorker implements MessageListener {

    private final MinioStorageService minioStorageService;
    private final UpdateInternalUtils updateInternalUtils;
    private final ObjectMapper objectMapper;
    private final VariantUtils variantUtils;


    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            FileJob job = objectMapper.readValue(message.getBody(), FileJob.class);
            log.info("Received PDF job for fileId: {} ({})", job.getFileId(), job.getObjectKey());
            processJob(job);
        } catch (Exception e) {
            log.error("Failed to process Redis PDF message: {}", e.getMessage(), e);
        }
    }

    private void processJob(FileJob job) {
        String status = FileStatus.COMPLETED.name();
        List<FileVariantDto> variants = new ArrayList<>();

        try (InputStream input = minioStorageService.getObject(job.getObjectKey());
             PDDocument document = PDDocument.load(input)) {

            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage firstPage = renderer.renderImageWithDPI(0, 150);

            variants = variantUtils.generateImageVariants(job.getObjectKey(), firstPage);

        } catch (Exception e) {
            status = FileStatus.FAILED.name();
            log.error("Error generating PDF thumbnail for fileId {}: {}", job.getFileId(), e.getMessage(), e);
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
