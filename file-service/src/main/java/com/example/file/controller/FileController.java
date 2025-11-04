package com.example.file.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.file.model.dto.FileResponseDto;
import com.example.file.repository.FileMetadataRepository;
import com.example.file.service.FileService;
import com.google.common.net.HttpHeaders;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;
    private final FileMetadataRepository fileMetadataRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam MultipartFile file,
            @RequestHeader("X-User") String userHeader) {
        return ResponseEntity.ok(fileService.uploadFile(file, userHeader));
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> download(@PathVariable UUID fileId) {
        byte[] data = fileService.downloadFile(fileId);
        String filename = fileMetadataRepository.findFilenameById(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(data);
    }

    @GetMapping({ "", "/" })
    public ResponseEntity<List<FileResponseDto>> listFiles(@RequestHeader("X-User") String userHeader) {
        return ResponseEntity.ok(fileService.listFiles(userHeader));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable UUID fileId, @RequestHeader("X-User") String userHeader) {
        fileService.deleteFile(fileId, userHeader);
        return ResponseEntity.noContent().build();
    }
}
