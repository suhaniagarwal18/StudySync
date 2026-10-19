-- ==============================================================================
-- StudySync Database Schema (MySQL 8 / InnoDB / utf8mb4)
-- Peer Study Buddy & Notes Exchange System
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS studysync
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE studysync;

-- Drop tables in dependency-safe reverse order
DROP TABLE IF EXISTS ratings;
DROP TABLE IF EXISTS session_participants;
DROP TABLE IF EXISTS study_sessions;
DROP TABLE IF EXISTS study_requests;
DROP TABLE IF EXISTS notes;
DROP TABLE IF EXISTS student_skills;
DROP TABLE IF EXISTS skills;
DROP TABLE IF EXISTS users;

-- ------------------------------------------------------------------------------
-- 1. USERS Table
-- Stores credentials and profiles for both Students and Admins.
-- (Design Note: For an Admin, adminId equals user_id via single-table user model)
-- ------------------------------------------------------------------------------
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    role ENUM('STUDENT', 'ADMIN') NOT NULL DEFAULT 'STUDENT',
    bio VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- 2. SKILLS Table
-- Master dictionary of normalized academic skills and subjects
-- ------------------------------------------------------------------------------
CREATE TABLE skills (
    skill_id INT AUTO_INCREMENT PRIMARY KEY,
    skill_name VARCHAR(80) NOT NULL UNIQUE,
    INDEX idx_skills_name (skill_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- 3. STUDENT_SKILLS Table
-- Maps students to skills they know or need help with.
-- ------------------------------------------------------------------------------
CREATE TABLE student_skills (
    user_id INT NOT NULL,
    skill_id INT NOT NULL,
    skill_type ENUM('KNOWN', 'NEEDED') NOT NULL,
    PRIMARY KEY (user_id, skill_id, skill_type),
    CONSTRAINT fk_student_skills_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_student_skills_skill FOREIGN KEY (skill_id)
        REFERENCES skills (skill_id) ON DELETE CASCADE,
    INDEX idx_student_skills_lookup (skill_id, skill_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- 4. NOTES Table
-- Stores metadata for student-uploaded lecture notes and study material.
-- ------------------------------------------------------------------------------
CREATE TABLE notes (
    note_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    subject VARCHAR(80) NOT NULL,
    module VARCHAR(50),
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notes_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE CASCADE,
    INDEX idx_notes_subject (subject),
    INDEX idx_notes_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- 5. STUDY_REQUESTS Table
-- Study buddy requests sent from one student to another for a specific subject.
-- ------------------------------------------------------------------------------
CREATE TABLE study_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    subject VARCHAR(80) NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_requests_sender FOREIGN KEY (sender_id)
        REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_requests_receiver FOREIGN KEY (receiver_id)
        REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT chk_sender_not_receiver CHECK (sender_id <> receiver_id),
    INDEX idx_requests_receiver_status (receiver_id, status),
    INDEX idx_requests_sender (sender_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- 6. STUDY_SESSIONS Table
-- Scheduled peer study sessions created from accepted study requests.
-- ------------------------------------------------------------------------------
CREATE TABLE study_sessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NOT NULL,
    subject VARCHAR(80) NOT NULL,
    session_date DATE NOT NULL,
    session_time TIME NOT NULL,
    status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    CONSTRAINT fk_sessions_request FOREIGN KEY (request_id)
        REFERENCES study_requests (request_id) ON DELETE CASCADE,
    INDEX idx_sessions_status (status),
    INDEX idx_sessions_date (session_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- 7. SESSION_PARTICIPANTS Table
-- Maps participants (students) involved in a study session.
-- ------------------------------------------------------------------------------
CREATE TABLE session_participants (
    session_id INT NOT NULL,
    student_id INT NOT NULL,
    PRIMARY KEY (session_id, student_id),
    CONSTRAINT fk_participants_session FOREIGN KEY (session_id)
        REFERENCES study_sessions (session_id) ON DELETE CASCADE,
    CONSTRAINT fk_participants_student FOREIGN KEY (student_id)
        REFERENCES users (user_id) ON DELETE CASCADE,
    INDEX idx_participants_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------
-- 8. RATINGS Table
-- Feedback and 1-5 star ratings given by session participants to each other.
-- ------------------------------------------------------------------------------
CREATE TABLE ratings (
    rating_id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    reviewer_id INT NOT NULL,
    reviewee_id INT NOT NULL,
    rating_value INT NOT NULL,
    feedback VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ratings_session FOREIGN KEY (session_id)
        REFERENCES study_sessions (session_id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_reviewer FOREIGN KEY (reviewer_id)
        REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_reviewee FOREIGN KEY (reviewee_id)
        REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT chk_rating_range CHECK (rating_value BETWEEN 1 AND 5),
    CONSTRAINT chk_reviewer_not_reviewee CHECK (reviewer_id <> reviewee_id),
    CONSTRAINT uq_session_reviewer_reviewee UNIQUE (session_id, reviewer_id, reviewee_id),
    INDEX idx_ratings_reviewee (reviewee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
