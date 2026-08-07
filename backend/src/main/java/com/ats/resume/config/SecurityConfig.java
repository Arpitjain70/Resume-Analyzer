package com.ats.resume.config;

import com.ats.resume.security.JwtAuthenticationFilter;
import com.ats.resume.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * ─────────────────────────────────────────────────────────
 * SecurityConfig — The Master Security Configuration
 * Location: config/SecurityConfig.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * By default, Spring Security locks down EVERYTHING. No request gets through
 * without authentication. We need to customize it to:
 * 1. Allow public access to /api/auth/** (login, register)
 * 2. Require JWT for all other endpoints
 * 3. Disable session cookies (we're stateless with JWT)
 * 4. Disable CSRF (not needed for REST APIs with JWT)
 * 5. Configure CORS (allow React app to call our API)
 * 6. Register our JWT filter in the filter chain
 *
 * @Configuration → This class defines Spring beans (@Bean methods)
 * @EnableWebSecurity → Activates Spring Security's web security support
 *
 * IMPORTANT CONCEPTS:
 *
 * @Bean → Tells Spring to manage the return value of this method as a singleton.
 *         Other classes can inject it using @Autowired or constructor injection.
 *
 * BCryptPasswordEncoder:
 *   BCrypt is a slow hashing algorithm designed for passwords.
 *   "Slow" is intentional — makes brute-force attacks take thousands of years.
 *   Even if your DB is hacked, attackers can't recover the passwords.
 *
 * DaoAuthenticationProvider:
 *   Connects Spring Security's authentication system to our database.
 *   "Dao" = Data Access Object — it loads users from our DB via UserDetailsService.
 *
 * STATELESS SESSION:
 *   SessionCreationPolicy.STATELESS → Spring Security won't create/use HTTP sessions.
 *   Each request must authenticate itself via JWT. No cookies. No state on server.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Main security filter chain — defines all security rules.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── CSRF ──────────────────────────────────────────────
            // Disable CSRF protection.
            // CSRF attacks exploit browser cookie behavior.
            // Since we use JWT in headers (not cookies), CSRF doesn't apply.
            .csrf(AbstractHttpConfigurer::disable)

            // ── CORS ──────────────────────────────────────────────
            // Enable CORS with our custom configuration (defined below).
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ── AUTHORIZATION RULES ───────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Allow anyone to access login and register endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // All other endpoints require authentication (valid JWT)
                .anyRequest().authenticated()
            )

            // ── SESSION ───────────────────────────────────────────
            // STATELESS: Don't create HTTP sessions. Each request is self-contained.
            // The JWT carries all the info we need to identify the user.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ── AUTHENTICATION PROVIDER ───────────────────────────
            // Register our custom provider that loads users from DB
            .authenticationProvider(authenticationProvider())

            // ── ADD OUR JWT FILTER ────────────────────────────────
            // Insert JwtAuthenticationFilter BEFORE Spring's default
            // UsernamePasswordAuthenticationFilter.
            // This way, our JWT is validated first.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS Configuration.
     * CORS (Cross-Origin Resource Sharing) prevents browsers from making requests
     * to a different domain/port than the page was loaded from.
     *
     * Our React app runs on localhost:5173.
     * Our API runs on localhost:8080.
     * Without CORS config, the browser would block the React app's API calls.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Only allow requests from our React app (or wherever it's deployed)
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .toList();
        configuration.setAllowedOrigins(origins);
        // Allow these HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Allow these headers (including Authorization where JWT goes)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        // Allow cookies/credentials if needed
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // Apply to all paths
        return source;
    }

    /**
     * AuthenticationProvider connects:
     * - UserDetailsService (loads user from DB)
     * - PasswordEncoder (verifies the password)
     *
     * DaoAuthenticationProvider is a standard implementation that:
     * 1. Calls userDetailsService.loadUserByUsername(email)
     * 2. Calls passwordEncoder.matches(rawPassword, storedHashedPassword)
     * 3. Returns an authenticated token if both match
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * BCryptPasswordEncoder — our password hasher.
     * Used to hash passwords before saving and to verify passwords during login.
     *
     * Strength 10 (default) means 2^10 = 1024 hashing rounds. Good balance of
     * security vs. performance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager — the entry point for programmatic authentication.
     * We inject this into AuthService to authenticate login credentials.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
