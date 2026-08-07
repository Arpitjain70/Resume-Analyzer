package com.ats.resume.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ─────────────────────────────────────────────────────────
 * User Entity
 * Location: entity/User.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * An "Entity" in Spring/JPA is a Java class that maps directly to a
 * database table. When Spring sees @Entity on a class, it knows this
 * class represents a table row, and each field is a column.
 *
 * KEY ANNOTATIONS:
 *
 * @Entity           → Marks this as a JPA entity (maps to a DB table)
 * @Table             → Specifies the exact table name ("users")
 * @Id               → This field is the primary key
 * @GeneratedValue   → MySQL auto-increments this (1, 2, 3, ...)
 * @Column           → Customizes column properties (name, nullable, unique)
 * @CreationTimestamp → Automatically sets the time when a record is created
 *
 * LOMBOK ANNOTATIONS (reduce boilerplate):
 * @Data             → Generates getters, setters, toString, equals, hashCode
 * @Builder          → Lets us use User.builder().name("John").build() pattern
 * @NoArgsConstructor → Generates User() constructor (required by JPA)
 * @AllArgsConstructor → Generates constructor with all fields (used by @Builder)
 *
 * DATABASE FLOW:
 * Java Object (User) → Hibernate → SQL INSERT INTO users (...) VALUES (...)
 * SQL row → Hibernate → Java Object (User)
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Primary key — auto-incremented by MySQL.
     * IDENTITY strategy means MySQL generates the ID, not Java.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User's full name.
     * nullable=false → MySQL enforces NOT NULL constraint.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Email is the login username.
     * unique=true → MySQL adds a UNIQUE INDEX, so two users can't have the same email.
     * nullable=false → Required field.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Hashed password. We NEVER store plain text passwords.
     * BCryptPasswordEncoder will hash it before saving.
     * Example: "mypassword123" → "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     */
    @Column(nullable = false)
    private String password;

    /**
     * Automatically set to NOW() when a User record is inserted.
     * updatable=false → Hibernate won't change this column on UPDATE.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * JPA lifecycle callback: runs automatically just BEFORE this entity
     * is first saved (persisted) to the database.
     * This is how we auto-set the createdAt timestamp.
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
