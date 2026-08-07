package com.ats.resume.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ─────────────────────────────────────────────────────────
 * LoginRequest DTO
 * Location: dto/LoginRequest.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Carries the login credentials from the HTTP request body to the service.
 * Only needs email + password (unlike RegisterRequest which also has name).
 *
 * REQUEST FLOW:
 * POST /api/auth/login
 *   Body: { "email": "john@example.com", "password": "secret123" }
 *   → Jackson deserializes JSON into LoginRequest
 *   → @Valid triggers validation
 *   → AuthService.login(loginRequest) is called
 *   → Returns JWT token if credentials are correct
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
