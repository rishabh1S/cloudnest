package com.example.link.service;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.link.exception.LinkNotFoundException;
import com.example.link.model.dto.LinkRequest;
import com.example.link.model.dto.LinkResponse;
import com.example.link.model.entity.FileMetadata;
import com.example.link.model.entity.Link;
import com.example.link.model.entity.User;
import com.example.link.repository.FileMetadataRepository;
import com.example.link.repository.LinkRepository;
import com.example.link.utils.JsonUtils;
import com.example.link.utils.ShortTokenGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LinkService {
    private final LinkRepository linkRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${base.service.url}")
    private String baseUrl;

    public LinkResponse createLink(LinkRequest req, String userHeader) {
        String token = ShortTokenGenerator.generate(7);
        User user = JsonUtils.fromJson(userHeader, User.class);

        Link link = new Link();
        FileMetadata file = fileMetadataRepository.findById(req.fileId())
        .orElseThrow(() -> new RuntimeException("No file found"));
        link.setFile(file);
        link.setToken(token);
        link.setExpiresAt(req.expiresAt());
        link.setOwner(user);

        if (req.password() != null && !req.password().isBlank()) {
            link.setPasswordHash(passwordEncoder.encode(req.password()));
        }

        linkRepository.save(link);

        return new LinkResponse(
                token,
                baseUrl + "/links/" + token,
                link.getExpiresAt());
    }

    public ResponseEntity<?> accessFile(String token, String password) {
        Link link = linkRepository.findByToken(token)
                .orElseThrow(() -> new LinkNotFoundException("Link not found"));

        if (link.getExpiresAt() != null && link.getExpiresAt().isBefore(Instant.now())) {
            return ResponseEntity.status(410).body("Link expired");
        }

        if (link.getPasswordHash() != null
                && (password == null || !passwordEncoder.matches(password, link.getPasswordHash()))) {
            return ResponseEntity.status(401).body("Invalid password");
        }

        FileMetadata metadata = fileMetadataRepository.findById(link.getFile().getId())
                .orElseThrow(() -> new RuntimeException("File not found"));
        String redirectUrl = metadata.getUrl();
        
        link.setViewCount(link.getViewCount() + 1);
        linkRepository.save(link);

        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }

}
