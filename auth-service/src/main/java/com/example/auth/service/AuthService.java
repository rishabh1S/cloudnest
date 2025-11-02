package com.example.auth.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.auth.config.JwtUtil;
import com.example.auth.exception.InvalidCredentialsException;
import com.example.auth.exception.UserAlreadyExistsException;
import com.example.auth.exception.UserNotFoundException;
import com.example.auth.model.dto.AuthenticatedUser;
import com.example.auth.model.dto.LoginRequest;
import com.example.auth.model.dto.LoginResponse;
import com.example.auth.model.dto.SignupRequest;
import com.example.auth.model.entity.User;
import com.example.auth.repository.UserRepository;
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

    public User signup(SignupRequest signupRequest) {
        log.info("Signing up user: {}", signupRequest.getEmail());
        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + signupRequest.getEmail() + " already exists");
        }

        User user = new User();
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Logging in user: {}", loginRequest.getEmail());
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }
        String token = jwtUtil.generateToken(user);
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        return response;
    }

    public boolean verifyToken(String authHeader) {
        log.info("Verifying token: {}", authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return false;
        String token = authHeader.substring(7);
        try {
            jwtUtil.isTokenValid(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String verifyAndExtractUser(String authHeader) {
        log.info("Verifying token: {}", authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.isTokenValid(token)) {
                throw new RuntimeException("Invalid token");
            }

            UUID id = jwtUtil.extractUserId(token);
            String name = jwtUtil.extractName(token);
            String email = jwtUtil.extractEmail(token);

            AuthenticatedUser user = new AuthenticatedUser(id, name, email);

            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(user); // serialize to JSON
        } catch (Exception e) {
            log.error("Token verification failed", e);
            throw new RuntimeException("Token verification failed");
        }
    }

}
