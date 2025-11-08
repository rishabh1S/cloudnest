package com.example.file.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.file.model.dto.FileUpdateRequest;
import com.example.file.service.FileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {
    private final FileService fileService;

    @PostMapping("/update")
    public ResponseEntity<Void> updateFileFromWorker(@RequestBody FileUpdateRequest req) {
        fileService.updateFileVariants(req);
        return ResponseEntity.ok().build();
    }
}
