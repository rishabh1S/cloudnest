package com.example.worker_service.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.springframework.stereotype.Component;

import com.example.worker_service.exception.ImageProcessingException;
import com.example.worker_service.model.dto.FileVariantDto;
import com.example.worker_service.service.MinioStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class VariantUtils {

    private final MinioStorageService minioStorageService;

    // Common image/video variant definitions
    private static final Map<String, Integer> VARIANT_SIZES = Map.of(
            "medium", 800,
            "thumbnail", 200);

    /**
     * Generate and upload all variants for a given image (BufferedImage).
     */
    public List<FileVariantDto> generateImageVariants(String objectKey, BufferedImage original) {
        List<FileVariantDto> variants = new ArrayList<>();

        try {
            for (Map.Entry<String, Integer> entry : VARIANT_SIZES.entrySet()) {
                String variantKey = entry.getKey();
                int size = entry.getValue();

                BufferedImage resized = Scalr.resize(original, size);
                BufferedImage rgb = new BufferedImage(resized.getWidth(), resized.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                rgb.getGraphics().drawImage(resized, 0, 0, null);
                String variantObjectKey = VariantUtils.replaceExtension(objectKey, "png");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(rgb, "png", baos);
                try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
                    minioStorageService.uploadVariantStream(is, baos.size(), variantObjectKey, variantKey, "image/png");
                }

                variants.add(buildVariantDto(variantObjectKey, variantKey));
            }

            // Original
            variants.add(buildVariantDto(objectKey, "original"));
        } catch (Exception e) {
            throw new ImageProcessingException("Failed to generate variants for " + objectKey, e);
        }

        return variants;
    }

    /**
     * Generate and upload image variants from a video thumbnail image file.
     */
    public List<FileVariantDto> generateVideoThumbnailVariants(String objectKey, BufferedImage thumbnail) {
        return generateImageVariants(objectKey, thumbnail);
    }

    /**
     * Build FileVariantDto with URL, size, and transform info.
     */
    private FileVariantDto buildVariantDto(String objectKey, String variantKey) {
        long size = minioStorageService.getSize(objectKey, variantKey);
        String url = minioStorageService.buildUrl(objectKey, variantKey);

        Map<String, Object> transform = variantKey.equals("original")
                ? Map.of()
                : Map.of("w", VARIANT_SIZES.getOrDefault(variantKey, 0));

        return FileVariantDto.builder()
                .variantKey(variantKey)
                .url(url)
                .transform(transform)
                .sizeBytes(size)
                .build();
    }

    public static String replaceExtension(String objectKey, String newExtension) {
        int lastDot = objectKey.lastIndexOf('.');
        if (lastDot != -1) {
            return objectKey.substring(0, lastDot) + "." + newExtension;
        }
        return objectKey + "." + newExtension;
    }

}
