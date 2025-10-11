package com.example.link.controller;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.link.model.entity.Link;
import com.example.link.service.LinkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinkController {
    private final LinkService linkService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateLink(@RequestBody Map<String, Object> request) {
        UUID fileId = UUID.fromString((String) request.get("file_id"));
        boolean isPublic = request.get("is_public") != null ? (Boolean) request.get("is_public") : true;
        Long expiresIn = request.get("expires_in") != null ? Long.valueOf(request.get("expires_in").toString()) : null;

        Link link = linkService.generateLink(fileId, isPublic, expiresIn);

        return ResponseEntity.ok(Map.of(
                "link", "http://localhost:8080/links/" + link.getToken(),
                "expires_at", link.getExpiresAt()));
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> getLink(@PathVariable String token) {
        Optional<Link> linkOpt = linkService.getLink(token);

        if (linkOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Link expired or invalid"));
        }

        Link link = linkOpt.get();

        // Return file info (frontend/file service will handle download)
        return ResponseEntity.ok(Map.of(
                "file_id", link.getFileId(),
                "download_count", link.getDownloadCount()));
    }
}
