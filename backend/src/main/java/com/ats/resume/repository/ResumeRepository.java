package com.ats.resume.repository;

import com.ats.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ─────────────────────────────────────────────────────────
 * ResumeRepository
 * Location: repository/ResumeRepository.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Handles all database operations for the Resume entity.
 * We need to find resumes by user, find the latest one, etc.
 *
 * CUSTOM QUERIES EXPLAINED:
 *
 * findByUserIdOrderByCreatedAtDesc:
 *   Spring reads: "find by [userId] order by [createdAt] [Desc]ending"
 *   SQL: SELECT * FROM resumes WHERE user_id = ? ORDER BY created_at DESC
 *   Returns a list of all resumes for a user, newest first.
 *
 * findTopByUserIdOrderByCreatedAtDesc:
 *   "Top" means LIMIT 1.
 *   SQL: SELECT * FROM resumes WHERE user_id = ? ORDER BY created_at DESC LIMIT 1
 *   Returns the most recently uploaded resume for a user.
 *
 * findByIdAndUserId:
 *   SQL: SELECT * FROM resumes WHERE id = ? AND user_id = ?
 *   Security check: ensures a user can only access THEIR OWN resumes,
 *   not someone else's (prevents horizontal privilege escalation).
 */
@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    /**
     * Get all resumes for a specific user, newest first.
     * Used in the dashboard history list.
     */
    List<Resume> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Get only the most recent resume for a user.
     * Used in the dashboard "latest resume" card.
     */
    Optional<Resume> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Get a specific resume, but only if it belongs to the given user.
     * This prevents User A from accessing User B's resume by guessing the ID.
     */
    Optional<Resume> findByIdAndUserId(Long id, Long userId);
}
