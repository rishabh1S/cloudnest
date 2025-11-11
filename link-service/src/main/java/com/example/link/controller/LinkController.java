package com.example.link.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
        @RequestBody LinkRequest request,
        @RequestHeader("X-User") String userHeader
    ) {
        return ResponseEntity.ok(linkService.createLink(request, userHeader));
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> accessLink(
        @PathVariable String token,
        @RequestParam(required = false) String password
    ) {
        return linkService.accessFile(token, password);
    }
}
