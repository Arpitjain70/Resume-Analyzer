package com.ats.resume.repository;

import com.ats.resume.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ─────────────────────────────────────────────────────────
 * UserRepository
 * Location: repository/UserRepository.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * We need to read/write User data from MySQL. Instead of writing
 * raw SQL or JDBC code, Spring Data JPA does it for us.
 *
 * HOW IT WORKS:
 * We extend JpaRepository<User, Long>:
 *   - User  → the entity this repository manages
 *   - Long  → the type of the primary key (id is Long)
 *
 * By extending JpaRepository, we GET FOR FREE:
 *   - save(user)           → INSERT or UPDATE
 *   - findById(id)         → SELECT WHERE id = ?
 *   - findAll()            → SELECT * FROM users
 *   - delete(user)         → DELETE WHERE id = ?
 *   - count()              → SELECT COUNT(*)
 *   - existsById(id)       → SELECT EXISTS(...)
 *   ... and many more
 *
 * CUSTOM METHODS:
 * Spring Data JPA reads method names and generates SQL automatically!
 * findByEmail(email) → SELECT * FROM users WHERE email = ?
 * existsByEmail(email) → SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)
 *
 * Optional<User> → might return a User, might return empty (no null pointer issues).
 *
 * @Repository → marks this as a Spring-managed component (bean).
 * Actually optional here since JpaRepository already registers it, but good practice.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     * Used during login to look up the user.
     *
     * Spring generates: SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if an email is already registered.
     * Used during registration to prevent duplicate accounts.
     *
     * Spring generates: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);
}
