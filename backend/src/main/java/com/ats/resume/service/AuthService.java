package com.ats.resume.service;

import com.ats.resume.dto.AuthResponse;
import com.ats.resume.dto.LoginRequest;
import com.ats.resume.dto.RegisterRequest;
import com.ats.resume.entity.User;
import com.ats.resume.repository.UserRepository;
import com.ats.resume.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * ─────────────────────────────────────────────────────────
 * AuthService — Authentication Business Logic
 * Location: service/AuthService.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Controllers should only handle HTTP (receive request, send response).
 * Business logic (validate email, hash password, create user, generate token)
 * belongs in a Service layer.
 *
 * This separation is called "Separation of Concerns":
 * Controller → receives HTTP request
 * Service    → runs business logic
 * Repository → talks to database
 *
 * @Service → Marks this as a Spring-managed service bean.
 * @Slf4j   → Adds a 'log' field for logging.
 *
 * REGISTRATION FLOW:
 * 1. Check if email already exists in DB
 * 2. Hash the password with BCrypt
 * 3. Create User entity and save to DB
 * 4. Generate JWT token
 * 5. Return AuthResponse with token + user info
 *
 * LOGIN FLOW:
 * 1. AuthenticationManager.authenticate() is called
 *    → It calls UserDetailsService.loadUserByUsername(email)
 *    → Then calls BCrypt.matches(rawPassword, hashedPassword)
 *    → Throws BadCredentialsException if wrong
 * 2. If auth succeeds, load UserDetails (for token generation)
 * 3. Generate JWT token
 * 4. Return AuthResponse
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    /**
     * Register a new user.
     *
     * @param request contains name, email, password (validated by @Valid in controller)
     * @return AuthResponse with JWT token and user info
     */
    public AuthResponse register(RegisterRequest request) {
        // Step 1: Make sure the email isn't already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        // Step 2: Build the User entity.
        // CRITICAL: Never save the raw password! Always hash it.
        // passwordEncoder.encode() runs BCrypt hashing.
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        // Step 3: Save to database
        // After save(), user.getId() is populated with the generated ID
        userRepository.save(user);
        log.info("New user registered: {}", request.getEmail());

        // Step 4: Load UserDetails and generate JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        // Step 5: Return response
        return new AuthResponse(token, user.getEmail(), user.getName());
    }

    /**
     * Authenticate an existing user.
     *
     * @param request contains email and password
     * @return AuthResponse with JWT token
     */
    public AuthResponse login(LoginRequest request) {
        // Step 1: Authenticate using Spring Security's AuthenticationManager.
        // Internally this:
        //   a. Calls loadUserByUsername(email) to get user from DB
        //   b. Calls BCrypt.matches(request.getPassword(), storedHash)
        //   c. Throws AuthenticationException if credentials are wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Step 2: If we reach here, authentication succeeded.
        // Load the full UserDetails for token generation.
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        // Step 3: Generate JWT token
        String token = jwtUtil.generateToken(userDetails);

        // Step 4: Load user entity to get name for response
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        log.info("User logged in: {}", request.getEmail());

        return new AuthResponse(token, user.getEmail(), user.getName());
    }
}
