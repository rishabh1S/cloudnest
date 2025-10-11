package com.example.link.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.link.model.entity.Link;
import com.example.link.repository.LinkRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LinkService {
    private final LinkRepository linkRepository;

    // Generate short link
    public Link generateLink(UUID fileId, boolean isPublic, Long expiresInSeconds) {
        Link link = new Link();
        link.setFileId(fileId);
        link.setPublic(isPublic);

        // Generate short token
        String token = UUID.randomUUID().toString().substring(0, 8);
        link.setToken(token);

        if (expiresInSeconds != null) {
            link.setExpiresAt(LocalDateTime.now().plusSeconds(expiresInSeconds));
        }

        return linkRepository.save(link);
    }

    // Resolve link
    public Optional<Link> getLink(String token) {
        Optional<Link> linkOpt = linkRepository.findByToken(token);

        if (linkOpt.isPresent()) {
            Link link = linkOpt.get();

            // Check expiration
            if (link.getExpiresAt() != null && LocalDateTime.now().isAfter(link.getExpiresAt())) {
                return Optional.empty();
            }

            // Increment download count
            link.setDownloadCount(link.getDownloadCount() + 1);
            linkRepository.save(link);

            return Optional.of(link);
        }

        return Optional.empty();
    }
}
