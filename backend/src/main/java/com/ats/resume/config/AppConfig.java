package com.ats.resume.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * ─────────────────────────────────────────────────────────
 * AppConfig — General Application Bean Definitions
 * Location: config/AppConfig.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Some objects need to be created once and shared across the whole application.
 * We define them as @Bean methods in @Configuration classes.
 * Spring creates them at startup and injects them wherever needed.
 *
 * RestTemplate:
 *   Spring's HTTP client. GeminiService uses it to call the Gemini API.
 *   By defining it as a @Bean, we get one shared instance (singleton)
 *   rather than creating a new one every time.
 *
 * ObjectMapper:
 *   Jackson's JSON parser/writer. Used in GeminiService to parse API responses.
 *   We register JavaTimeModule so it can serialize/deserialize
 *   Java 8 date/time types (LocalDateTime, LocalDate, etc.).
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate for making HTTP requests.
     * Used by GeminiService to call the Gemini AI API.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Jackson ObjectMapper for JSON serialization/deserialization.
     *
     * JavaTimeModule is required to properly handle:
     * - LocalDateTime
     * - LocalDate
     * - ZonedDateTime
     * Without it, these types would serialize as arrays [2024, 1, 15, 10, 30, 0]
     * instead of "2024-01-15T10:30:00".
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
