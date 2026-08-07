package com.ats.resume.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * ─────────────────────────────────────────────────────────
 * JwtUtil — JWT Token Generator and Validator
 * Location: security/JwtUtil.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * JWT (JSON Web Token) is how we prove a user is authenticated
 * without storing sessions on the server.
 *
 * HOW JWT WORKS (3 parts, separated by dots):
 * eyJhbGciOiJIUzI1NiJ9  ← Header (algorithm: HS256)
 * .
 * eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxN...  ← Payload (claims: email, expiry)
 * .
 * SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c  ← Signature (HMAC-SHA256 with secret key)
 *
 * WHY STATELESS?
 * Traditional sessions: server stores session data → hard to scale.
 * JWT: client stores the token → server just verifies the signature.
 * Any server can verify the token without shared session storage.
 *
 * AUTHENTICATION FLOW:
 * 1. User logs in → we generate a JWT with their email as "subject"
 * 2. Client stores token in localStorage
 * 3. Client sends token in every request: Authorization: Bearer <token>
 * 4. JwtAuthenticationFilter reads token, validates it, extracts email
 * 5. Spring Security knows who the user is for that request
 *
 * @Component → Makes this a Spring-managed singleton bean.
 * @Value     → Injects values from application.properties.
 */
@Component
public class JwtUtil {

    /**
     * The secret key used to sign and verify tokens.
     * Injected from jwt.secret in application.properties.
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * How long a token is valid (in milliseconds).
     * Injected from jwt.expiration (86400000 = 24 hours).
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Converts the raw secret string into a cryptographic Key object.
     * HMAC-SHA256 requires a key of at least 256 bits.
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Generate a JWT token for an authenticated user.
     *
     * TOKEN CONTENTS (claims):
     * - subject: user's email (used as username)
     * - issuedAt: when the token was created
     * - expiration: when the token expires
     *
     * @param userDetails Spring Security's representation of the logged-in user
     * @return JWT token string like "eyJhbGci..."
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return buildToken(claims, userDetails.getUsername());
    }

    private String buildToken(Map<String, Object> extraClaims, String subject) {
        return Jwts.builder()
                .setClaims(extraClaims)           // any extra data we want in the token
                .setSubject(subject)              // email is the "subject" (who this token is for)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // sign with our secret
                .compact();                       // serialize to compact string
    }

    /**
     * Extract the email (subject) from a JWT token.
     * Used in the filter to identify which user is making the request.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Check if a token is valid:
     * 1. Does the username in the token match the user we loaded from DB?
     * 2. Is the token still within its expiration time?
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Is the token past its expiration date?
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic helper to extract any "claim" from a token.
     * A "claim" is any piece of data stored inside the JWT payload.
     *
     * claimsResolver: a function that takes all claims and returns the one we want.
     * Example: Claims::getSubject → returns the subject claim (email).
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse and verify the token signature, then return all claims.
     * Throws JwtException if the token is tampered with, expired, or malformed.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
