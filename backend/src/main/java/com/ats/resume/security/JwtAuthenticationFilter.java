package com.ats.resume.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ─────────────────────────────────────────────────────────
 * JwtAuthenticationFilter — Runs on Every HTTP Request
 * Location: security/JwtAuthenticationFilter.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * When a client sends a request to a protected endpoint, we need to:
 * 1. Read the JWT from the Authorization header
 * 2. Validate it
 * 3. Load the user from DB
 * 4. Tell Spring Security "this request is authenticated"
 *
 * This filter intercepts EVERY HTTP request before it reaches any controller.
 *
 * OncePerRequestFilter: Base class that guarantees this filter runs exactly ONCE
 * per request (Spring can sometimes call filters multiple times without this).
 *
 * HOW SPRING SECURITY FILTERS WORK:
 * HTTP Request → Filter Chain (many filters in sequence) → Controller
 *
 * Our filter is inserted into this chain before Spring Security's own
 * authentication filter.
 *
 * STEP BY STEP FLOW:
 * 1. Request arrives: GET /api/resume/history
 *    Headers: { Authorization: "Bearer eyJhbGci..." }
 *
 * 2. doFilterInternal() runs:
 *    a. Extract "Authorization" header
 *    b. Check it starts with "Bearer "
 *    c. Extract the token part (after "Bearer ")
 *    d. Extract email from token
 *    e. Load user from DB by email
 *    f. Validate token (correct user? not expired?)
 *    g. Create UsernamePasswordAuthenticationToken
 *    h. Put it in SecurityContextHolder
 *
 * 3. Request continues to the controller
 *    The controller can now call SecurityContextHolder to know who's logged in
 *
 * @Slf4j → Lombok generates a 'log' field for logging (log.debug, log.error)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Step 1: Read the Authorization header
        final String authHeader = request.getHeader("Authorization");

        // Step 2: If no Authorization header, or it doesn't start with "Bearer ",
        //         skip JWT validation and pass to the next filter.
        //         Public endpoints (/api/auth/**) will be handled here.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract the token (everything after "Bearer ")
        // "Bearer eyJhbGciOiJIUzI1NiJ9..." → "eyJhbGciOiJIUzI1NiJ9..."
        final String jwt = authHeader.substring(7);

        try {
            // Step 4: Extract the email (username) from the token
            final String userEmail = jwtUtil.extractUsername(jwt);

            // Step 5: Only authenticate if we got an email AND
            //         the user isn't already authenticated in this request
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Step 6: Load the user from the database
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // Step 7: Check if the token is valid (matches user + not expired)
                if (jwtUtil.isTokenValid(jwt, userDetails)) {

                    // Step 8: Create an Authentication object.
                    // UsernamePasswordAuthenticationToken = Spring Security's way of saying
                    // "I know who this user is, and here are their permissions."
                    // 3rd argument is the list of authorities (roles).
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,                        // credentials (null because we already verified via JWT)
                            userDetails.getAuthorities() // roles/permissions
                    );

                    // Attach request details (IP address, session info) to the auth token
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Step 9: Store authentication in the SecurityContext.
                    // Now Spring Security knows this request is authenticated.
                    // Controllers can use @AuthenticationPrincipal to access this.
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Authenticated user: {}", userEmail);
                }
            }
        } catch (Exception e) {
            // Token is invalid (expired, tampered, etc.)
            // We just log it and let the request continue without authentication.
            // Spring Security will then reject it with 401 Unauthorized.
            log.error("JWT authentication failed: {}", e.getMessage());
        }

        // Step 10: Pass to the next filter (or controller if all filters pass)
        filterChain.doFilter(request, response);
    }
}
