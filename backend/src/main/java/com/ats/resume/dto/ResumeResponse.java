package com.ats.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ─────────────────────────────────────────────────────────
 * ResumeResponse DTO
 * Location: dto/ResumeResponse.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * When the frontend requests resume data, we don't return the
 * entire Resume entity (it has lazy-loaded relations, internal
 * fields like filePath that clients don't need to see).
 *
 * This DTO is the clean, safe representation we send to the client.
 * filePath is intentionally NOT included here — security best practice.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponse {
    private Long id;
    private String originalFileName;
    private Integer atsScore;
    private LocalDateTime createdAt;
}
