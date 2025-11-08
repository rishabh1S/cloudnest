package com.example.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth.model.dto.ForgotPasswordRequest;
import com.example.auth.model.dto.LoginRequest;
import com.example.auth.model.dto.LoginResponse;
import com.example.auth.model.dto.PasswordResetRequest;
import com.example.auth.model.dto.SignupRequest;
import com.example.auth.model.entity.User;
import com.example.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<User> signup(@RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> requestPasswordReset(@RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request.email());
        return ResponseEntity.ok("If the email exists, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest request, @RequestParam("token") String token) {
        authService.resetPassword(token, request.newPassword());
        return ResponseEntity.ok("Password successfully reset");
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
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
