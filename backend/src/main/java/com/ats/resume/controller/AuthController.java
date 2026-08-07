package com.ats.resume.controller;

import com.ats.resume.dto.AuthResponse;
import com.ats.resume.dto.LoginRequest;
import com.ats.resume.dto.RegisterRequest;
import com.ats.resume.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ─────────────────────────────────────────────────────────
 * AuthController — HTTP Entry Points for Authentication
 * Location: controller/AuthController.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * The Controller is the front door of our API.
 * It receives HTTP requests, delegates to the Service, and returns responses.
 * Controllers should have ZERO business logic — just HTTP handling.
 *
 * KEY ANNOTATIONS:
 *
 * @RestController = @Controller + @ResponseBody
 *   → @Controller: marks this as a web controller (Spring manages it)
 *   → @ResponseBody: automatically converts return values to JSON using Jackson
 *
 * @RequestMapping("/api/auth")
 *   → All endpoints in this class are prefixed with /api/auth
 *   → Register: POST /api/auth/register
 *   → Login:    POST /api/auth/login
 *
 * @PostMapping → maps HTTP POST requests to this method
 *
 * @RequestBody → tells Spring to deserialize the JSON request body into the DTO
 *
 * @Valid → triggers Bean Validation on the DTO fields (@NotBlank, @Email, etc.)
 *         If validation fails, MethodArgumentNotValidException is thrown,
 *         which GlobalExceptionHandler catches and returns proper error JSON.
 *
 * ResponseEntity<T> → wraps the response body + HTTP status code
 *   ResponseEntity.ok(body) → 200 OK with body
 *   ResponseEntity.status(201).body(body) → 201 Created with body
 *
 * REQUEST → RESPONSE FLOW:
 * POST /api/auth/register
 *   → JSON body deserialized → RegisterRequest
 *   → @Valid validates fields
 *   → authService.register(request) runs business logic
 *   → Returns AuthResponse as JSON with 201 status
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user account.
     *
     * URL: POST http://localhost:8080/api/auth/register
     * Body: { "name": "John", "email": "john@example.com", "password": "password123" }
     *
     * Response (201 Created):
     * { "token": "eyJhbGci...", "email": "john@example.com", "name": "John" }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Login with existing credentials.
     *
     * URL: POST http://localhost:8080/api/auth/login
     * Body: { "email": "john@example.com", "password": "password123" }
     *
     * Response (200 OK):
     * { "token": "eyJhbGci...", "email": "john@example.com", "name": "John" }
     *
     * Response (401 Unauthorized if wrong credentials):
     * handled by Spring Security automatically
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
