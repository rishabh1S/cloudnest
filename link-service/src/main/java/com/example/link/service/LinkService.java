package com.example.link.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.link.exception.LinkNotFoundException;
import com.example.link.model.dto.AccessLinkResult;
import com.example.link.model.dto.LinkRequest;
import com.example.link.model.dto.LinkResponse;
import com.example.link.model.entity.FileMetadata;
import com.example.link.model.entity.Link;
import com.example.link.repository.FileMetadataRepository;
import com.example.link.repository.LinkRepository;
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

    public LinkResponse createLink(LinkRequest req) {
        String token = ShortTokenGenerator.generate(7);

        Link link = new Link();
        FileMetadata file = fileMetadataRepository.findById(req.fileId())
        .orElseThrow(() -> new RuntimeException("No file found"));
        link.setFile(file);
        link.setToken(token);
        link.setExpiresAt(req.expiresAt());

        if (req.password() != null && !req.password().isBlank()) {
            link.setPasswordHash(passwordEncoder.encode(req.password()));
        }
        String url = baseUrl + "/links/" + token;
        link.setUrl(url);

        linkRepository.save(link);

        return new LinkResponse(
                token,
                url,
                link.getExpiresAt());
    }

    public AccessLinkResult accessFile(String token, String password) {
        Link link = linkRepository.findByToken(token)
                .orElseThrow(() -> new LinkNotFoundException("Link not found"));

        if (link.getExpiresAt() != null && link.getExpiresAt().isBefore(Instant.now())) {
            return AccessLinkResult.expired();
        }

        if (link.getPasswordHash() != null &&
                (password == null || !passwordEncoder.matches(password, link.getPasswordHash()))) {
            return AccessLinkResult.invalidPassword();
        }

        String fileUrl = link.getFile().getUrl();

        // Increase views
        link.setViewCount(link.getViewCount() + 1);
        linkRepository.save(link);

        return AccessLinkResult.success(fileUrl);
    }

    public void deleteLink(UUID linkId){
        if(linkRepository.existsById(linkId)){
            linkRepository.deleteById(linkId);
        }
    }

}
