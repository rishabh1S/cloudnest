package com.example.auth.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.auth.config.JwtUtil;
import com.example.auth.exception.EmailSendFailedException;
import com.example.auth.exception.InvalidCredentialsException;
import com.example.auth.exception.InvalidTokenException;
import com.example.auth.exception.MissingAuthorizationHeaderException;
import com.example.auth.exception.UserAlreadyExistsException;
import com.example.auth.exception.UserNotFoundException;
import com.example.auth.model.dto.AuthResponse;
import com.example.auth.model.dto.AuthenticatedUser;
import com.example.auth.model.dto.LoginRequest;
import com.example.auth.model.dto.SignupRequest;
import com.example.auth.model.entity.PasswordResetToken;
import com.example.auth.model.entity.User;
import com.example.auth.repository.PasswordResetTokenRepository;
import com.example.auth.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final PasswordResetTokenRepository resetTokenRepository;

    @Value("${frontend.origin}")
    private String frontendOrigin;

    public AuthResponse signup(SignupRequest signupRequest) {
        log.info("Signing up user: {}", signupRequest.getEmail());
        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + signupRequest.getEmail() + " already exists");
        }

        User user = new User();
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        userRepository.save(user);
        AuthenticatedUser authUser = new AuthenticatedUser(user.getId(), user.getName(), user.getEmail());
        String token = jwtUtil.generateToken(user);
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUser(authUser);
        return response;
    }

    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Logging in user: {}", loginRequest.getEmail());
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }
        AuthenticatedUser authUser = new AuthenticatedUser(user.getId(), user.getName(), user.getEmail());
        String token = jwtUtil.generateToken(user);
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUser(authUser);
        return response;
    }

    public String verifyAndExtractUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new MissingAuthorizationHeaderException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.isTokenValid(token)) {
                throw new InvalidTokenException("Invalid or expired token");
            }

            UUID id = jwtUtil.extractUserId(token);
            String name = jwtUtil.extractName(token);
            String email = jwtUtil.extractEmail(token);

            AuthenticatedUser user = new AuthenticatedUser(id, name, email);

            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(user); // serialize to JSON
        } catch (JsonProcessingException | RuntimeException e) {
            log.error("Token verification failed", e);
            throw new InvalidTokenException("Token verification failed");
        }
    }

    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .used(false)
                .build();

        resetTokenRepository.save(resetToken);

        String resetLink = frontendOrigin + "/reset-password?token=" + token;

        String mailSubject = "Password Reset Request - CloudNest Account";

        String mailContent = """
                Dear %s,

                We received a request to reset the password for your CloudNest account.

                To proceed, please click the secure link below. This link will remain valid for 30 minutes:

                %s

                If you did not request a password reset, you can safely ignore this email — your account will remain secure.

                Best regards,
                The CloudNest Team
                """
                .formatted(user.getName() != null ? user.getName() : "User", resetLink);

        try {
            emailService.sendEmail(user.getEmail(), mailSubject, mailContent);
            log.info("Email sent successfully to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", user.getEmail(), e.getMessage(), e);
            throw new EmailSendFailedException("Failed to send password reset email. Please try again later.");
        }

    }

    public boolean resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Token expired or already used");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);

        return true;
    }

}
