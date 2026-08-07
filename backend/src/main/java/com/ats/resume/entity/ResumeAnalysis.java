package com.ats.resume.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * ─────────────────────────────────────────────────────────
 * ResumeAnalysis Entity
 * Location: entity/ResumeAnalysis.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Stores the AI-generated analysis result for a resume.
 * Kept separate from Resume so the Resume table stays lightweight.
 * parsedJson and suggestionsJson can be large TEXT fields.
 *
 * WHAT'S STORED:
 * - parsedJson: Extracted resume data (name, skills, experience, etc.) as JSON string
 *   Example: {"name":"John","email":"john@example.com","skills":["Java","React"]}
 *
 * - atsScore: Overall ATS score (0-100)
 *
 * - suggestionsJson: AI suggestions as JSON string
 *   Example: {"problems":["No quantified achievements"],"improvements":["Add metrics"]}
 *
 * WHY JSON STRINGS INSTEAD OF NESTED OBJECTS?
 * The AI returns flexible/variable structures. Storing as JSON strings is simpler
 * for a beginner project than creating separate entities for each suggestion.
 *
 * @OneToOne + @JoinColumn:
 * This entity OWNS the relationship (it has the foreign key column "resume_id").
 * The Resume entity has mappedBy="resume", pointing back here.
 */
@Entity
@Table(name = "resume_analysis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The resume this analysis belongs to.
     * @JoinColumn(name="resume_id") → creates the "resume_id" foreign key column here.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    /**
     * JSON string of parsed resume data from AI.
     * LONGTEXT in MySQL can store up to 4 GB of text.
     * columnDefinition overrides the default VARCHAR type.
     */
    @Column(name = "parsed_json", columnDefinition = "TEXT")
    private String parsedJson;

    /**
     * Overall ATS score (0-100).
     */
    @Column(name = "ats_score")
    private Integer atsScore;

    /**
     * JSON string with AI suggestions, top problems, missing skills.
     */
    @Column(name = "suggestions_json", columnDefinition = "TEXT")
    private String suggestionsJson;
}
