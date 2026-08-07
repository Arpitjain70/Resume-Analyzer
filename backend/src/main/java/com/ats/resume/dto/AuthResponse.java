package com.ats.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ─────────────────────────────────────────────────────────
 * AuthResponse DTO
 * Location: dto/AuthResponse.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * After successful login/register, we return a JSON response.
 * This DTO defines the shape of that response.
 *
 * The client (React) receives this and stores the token in localStorage.
 * On future requests, the client sends:
 *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 *
 * RESPONSE EXAMPLE:
 * {
 *   "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIn0...",
 *   "email": "john@example.com",
 *   "name": "John Doe"
 * }
 */
@Data
@AllArgsConstructor  // Generates constructor: AuthResponse(String token, String email, String name)
public class AuthResponse {
    private String token;
    private String email;
    private String name;
}
