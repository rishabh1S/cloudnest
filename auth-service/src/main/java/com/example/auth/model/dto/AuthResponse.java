package com.example.auth.model.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private AuthenticatedUser user;
}
