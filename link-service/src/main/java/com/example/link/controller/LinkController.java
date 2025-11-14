package com.example.link.controller;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.link.model.dto.LinkRequest;
import com.example.link.model.dto.LinkResponse;
import com.example.link.service.LinkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinkController {
    private final LinkService linkService;

    @PostMapping("/generate")
    public ResponseEntity<LinkResponse> createLink(
        @RequestBody LinkRequest request) {
        return ResponseEntity.ok(linkService.createLink(request));
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> accessLink(
            @PathVariable String token,
            @RequestParam(required = false) String password
    ) {
        var result = linkService.accessFile(token, password);

        if (result.isExpired()) {
            return ResponseEntity.status(410).body("Link expired");
        }

        if (result.isInvalidPassword()) {
            return ResponseEntity.status(401).body("Invalid password");
        }

        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, result.redirectUrl())
                .build();
    }

    @DeleteMapping("/delete/{linkId}")
    public ResponseEntity<Void> deleteLink(@PathVariable UUID linkId){
        linkService.deleteLink(linkId);
        return ResponseEntity.ok().build();
    }
}
