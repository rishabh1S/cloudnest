package com.example.cloudnest.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.cloudnest.config.security.JwtUtil;
import com.example.cloudnest.model.dto.LoginRequest;
import com.example.cloudnest.model.dto.SignupRequest;
import com.example.cloudnest.model.entity.User;
import com.example.cloudnest.repository.UserRepository;

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
            throw new RuntimeException("User with email " + signupRequest.getEmail() + " already exists");
        }

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        return userRepository.save(user);
    }

    public String login(LoginRequest loginRequest) {
        log.info("Logging in user: {}", loginRequest.getEmail());
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        return jwtUtil.generateToken(user.getEmail());
    }
}
