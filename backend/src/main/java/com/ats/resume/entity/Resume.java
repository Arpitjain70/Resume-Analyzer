package com.ats.resume.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ─────────────────────────────────────────────────────────
 * Resume Entity
 * Location: entity/Resume.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Represents one uploaded resume PDF. Each row stores metadata
 * about the file (where it is on disk, who uploaded it, when).
 * The actual AI analysis is stored in a separate ResumeAnalysis entity.
 *
 * RELATIONSHIP:
 * Resume → belongs to → User (many resumes can belong to one user)
 * Resume → has one → ResumeAnalysis (one analysis per resume)
 *
 * @ManyToOne:
 * Many Resume rows can point to ONE User row.
 * @JoinColumn(name="user_id") → the foreign key column in the resumes table.
 * FetchType.LAZY → DON'T load the User from DB when we load a Resume.
 * Only load it if we explicitly call resume.getUser(). This is more efficient.
 *
 * @OneToOne(mappedBy="resume"):
 * One Resume has exactly one ResumeAnalysis.
 * mappedBy="resume" means the foreign key is on the ResumeAnalysis side, not here.
 * cascade=CascadeType.ALL → if we delete a Resume, its ResumeAnalysis is also deleted.
 */
@Entity
@Table(name = "resumes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Which user uploaded this resume.
     * LAZY loading: we only fetch the User from DB when needed.
     * @JoinColumn: the "user_id" column in the resumes table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The original filename as uploaded by the user.
     * Example: "john_doe_resume.pdf"
     */
    @Column(name = "original_file_name")
    private String originalFileName;

    /**
     * Where the file is saved on the server's filesystem.
     * Example: "./uploads/resumes/1720000000000_john_doe_resume.pdf"
     */
    @Column(name = "file_path")
    private String filePath;

    /**
     * Quick-access ATS score (0-100).
     * Also stored in ResumeAnalysis, but we keep a copy here
     * so we can show it in lists without loading the full analysis.
     */
    @Column(name = "ats_score")
    private Integer atsScore;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * The analysis result for this resume.
     * OneToOne relationship — one resume has one analysis.
     * CascadeType.ALL means operations (save, delete) cascade to the analysis too.
     * orphanRemoval=true means if we remove the analysis from the resume object, it's deleted from DB.
     */
    @OneToOne(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ResumeAnalysis analysis;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.atsScore == null) {
            this.atsScore = 0;
        }
    }
}
