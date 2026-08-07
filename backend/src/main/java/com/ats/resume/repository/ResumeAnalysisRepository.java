package com.ats.resume.repository;

import com.ats.resume.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ─────────────────────────────────────────────────────────
 * ResumeAnalysisRepository
 * Location: repository/ResumeAnalysisRepository.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Manages DB operations for ResumeAnalysis (AI analysis results).
 *
 * findByResumeId:
 *   SQL: SELECT * FROM resume_analysis WHERE resume_id = ?
 *   Used to load the full analysis result for a specific resume.
 */
@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {

    Optional<ResumeAnalysis> findByResumeId(Long resumeId);
}
