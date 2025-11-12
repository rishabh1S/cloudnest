package com.example.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth.model.dto.ApiResponse;
import com.example.auth.model.dto.AuthResponse;
import com.example.auth.model.dto.ForgotPasswordRequest;
import com.example.auth.model.dto.LoginRequest;
import com.example.auth.model.dto.PasswordResetRequest;
import com.example.auth.model.dto.SignupRequest;
import com.example.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        return ResponseEntity.status(201).body(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> requestPasswordReset(@RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request.email());
        return ResponseEntity.ok(ApiResponse.builder()
                .message("If the email exists, a reset link has been sent.")
                .statusCode(200)
                .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody PasswordResetRequest request,
            @RequestParam String token) {
        authService.resetPassword(token, request.newPassword());
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Password reset is successful.")
                .statusCode(200)
                .build());
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String userJson = authService.verifyAndExtractUser(authHeader);

            return ResponseEntity.ok()
                    .header("X-User", userJson)
                    .build();

        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
}
