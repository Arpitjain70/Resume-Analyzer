package com.ats.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ─────────────────────────────────────────────────────────
 * AnalysisResponse DTO
 * Location: dto/AnalysisResponse.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Combines resume metadata + analysis data into one response
 * for the analysis detail page.
 *
 * parsedData and suggestions are kept as strings (raw JSON).
 * The React frontend will parse these JSON strings into objects
 * using JSON.parse().
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponse {
    private Long resumeId;
    private String originalFileName;
    private Integer atsScore;
    private String parsedData;       // JSON string: name, email, skills, etc.
    private String suggestions;      // JSON string: problems, improvements, missing skills
}
