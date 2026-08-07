-- =========================================================
-- ATS Resume Analyzer — Database Schema
-- =========================================================
-- Run these commands in your MySQL client BEFORE starting the backend.
-- You only need to create the DATABASE manually.
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) will create
-- the tables automatically when the app first starts.
-- =========================================================

-- Step 1: Create the database
CREATE DATABASE IF NOT EXISTS ats_resume_db
    CHARACTER SET utf8mb4       -- supports all Unicode characters including emojis
    COLLATE utf8mb4_unicode_ci; -- case-insensitive comparison

-- Step 2: Switch to that database
USE ats_resume_db;

-- ─────────────────────────────────────────────────────────
-- TABLE: users
-- Stores registered user accounts.
-- ─────────────────────────────────────────────────────────
-- NOTE: Hibernate will auto-create this table. This SQL is
-- for your reference and manual setup.
CREATE TABLE IF NOT EXISTS users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique user ID',
    name       VARCHAR(100)  NOT NULL             COMMENT 'Full name of the user',
    email      VARCHAR(150)  NOT NULL UNIQUE       COMMENT 'Email used to login (must be unique)',
    password   VARCHAR(255)  NOT NULL             COMMENT 'BCrypt hashed password (never plain text!)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'When the account was created'
) COMMENT 'Stores all registered users';

-- ─────────────────────────────────────────────────────────
-- TABLE: resumes
-- Each row = one PDF file uploaded by a user.
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS resumes (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT       NOT NULL COMMENT 'Which user uploaded this resume',
    original_file_name VARCHAR(255) NULL     COMMENT 'Original filename when uploaded',
    file_path          VARCHAR(500) NULL     COMMENT 'Where the PDF is stored on disk',
    ats_score          INT DEFAULT 0         COMMENT 'Quick-access ATS score (also stored in resume_analysis)',
    created_at         DATETIME DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key: if a user is deleted, their resumes are also deleted (CASCADE)
    CONSTRAINT fk_resume_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT 'Stores uploaded resume file metadata';

-- ─────────────────────────────────────────────────────────
-- TABLE: resume_analysis
-- Stores the AI analysis result for each resume.
-- One-to-one with resumes (one resume has one analysis).
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS resume_analysis (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id        BIGINT NOT NULL UNIQUE COMMENT 'One-to-one: each resume has exactly one analysis',
    parsed_json      LONGTEXT NULL         COMMENT 'Structured data extracted from PDF (name, email, skills...)',
    ats_score        INT DEFAULT 0         COMMENT 'Overall ATS score 0-100',
    suggestions_json LONGTEXT NULL         COMMENT 'AI suggestions, problems, missing skills as JSON',

    CONSTRAINT fk_analysis_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
) COMMENT 'Stores AI analysis results for each resume';

-- ─────────────────────────────────────────────────────────
-- Useful queries for debugging (run these anytime to check data)
-- ─────────────────────────────────────────────────────────

-- See all users:
-- SELECT id, name, email, created_at FROM users;

-- See all resumes with owner name:
-- SELECT r.id, u.name, r.original_file_name, r.ats_score, r.created_at
-- FROM resumes r JOIN users u ON r.user_id = u.id;

-- See analysis for a specific resume:
-- SELECT * FROM resume_analysis WHERE resume_id = 1;
