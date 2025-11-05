package com.example.file.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.file.model.entity.FileMetadata;
import com.example.file.repository.FileMetadataRepository;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThumbnailService {
    private final MinioClient minioClient;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.url}")
    private String minioUrl;

    @Async
    public void generateThumbnailAsync(FileMetadata file) {
        try (InputStream input = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(file.getObjectKey()).build())) {

            BufferedImage original = ImageIO.read(input);
            if (original == null) {
                log.warn("Could not read original image for file: {}", file.getObjectKey());
                return;
            }

            // Resize with Scalr
            BufferedImage thumbnail = Scalr.resize(original, 200);

            // Convert to RGB to avoid ImageIO write issues
            BufferedImage rgbThumbnail = new BufferedImage(thumbnail.getWidth(), thumbnail.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            rgbThumbnail.getGraphics().drawImage(thumbnail, 0, 0, null);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(rgbThumbnail, "jpg", baos);
            baos.flush();

            String baseName = file.getObjectKey().replaceAll("\\.[^.]+$", "");
            String thumbKey = "thumbnails/" + baseName + ".jpg";

            try (InputStream thumbStream = new ByteArrayInputStream(baos.toByteArray())) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(thumbKey)
                        .stream(thumbStream, baos.size(), -1)
                        .contentType("image/jpeg")
                        .build());
            }

            String thumbnailUrl = String.format("%s/%s/%s", minioUrl, bucket, thumbKey);

            file.setThumbnailUrl(thumbnailUrl);
            file.setFileStatus("READY");
            fileMetadataRepository.save(file);

        } catch (Exception e) {
            log.error("Thumbnail generation failed: {}", e.getMessage());
            file.setFileStatus("FAILED");
            fileMetadataRepository.save(file);
        }
    }
}
