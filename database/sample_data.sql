-- ==============================================================================
-- StudySync Sample Data (Realistic Academic Demonstration Records)
-- ==============================================================================
-- AUTHENTIC BCRYPT HASHES:
-- Generated via com.studysync.util.HashGenerator using jBCrypt (work factor 10)
-- Verified with PasswordUtil.verify(password, hash) == true
--
-- Credentials for Evaluation:
--   Admin:   admin@studysync.edu   / Admin@123
--   Students:
--     aarav@studysync.edu   / Student@123
--     diya@studysync.edu    / Student@123
--     rohan@studysync.edu   / Student@123
--     ananya@studysync.edu  / Student@123
--     vikram@studysync.edu  / Student@123
--     sneha@studysync.edu   / Student@123
-- ==============================================================================

USE studysync;

-- Disable foreign key checks for clean truncation and insertion
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE ratings;
TRUNCATE TABLE session_participants;
TRUNCATE TABLE study_sessions;
TRUNCATE TABLE study_requests;
TRUNCATE TABLE notes;
TRUNCATE TABLE student_skills;
TRUNCATE TABLE skills;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------------------------
-- 1. USERS (1 Admin + 6 Students across diverse departments)
-- ------------------------------------------------------------------------------
INSERT INTO users (user_id, name, email, password_hash, department, role, bio, is_active) VALUES
(1, 'Aarav Sharma', 'aarav@studysync.edu', '$2a$10$eSluIiiL/O3Qhts28/MxAOR0HeDFkOxkrDgatrVf6bicqyKm/wHVe', 'Computer Science', 'STUDENT', 'Third year CS student passionate about backend systems, DBMS, and Python programming.', TRUE),
(2, 'Diya Patel', 'diya@studysync.edu', '$2a$10$eSluIiiL/O3Qhts28/MxAOR0HeDFkOxkrDgatrVf6bicqyKm/wHVe', 'Information Technology', 'STUDENT', 'Enthusiastic about Core Java, algorithms, and data structures. Eager to master relational databases.', TRUE),
(3, 'Rohan Verma', 'rohan@studysync.edu', '$2a$10$eSluIiiL/O3Qhts28/MxAOR0HeDFkOxkrDgatrVf6bicqyKm/wHVe', 'Electronics', 'STUDENT', 'Hardware and systems lover focusing on OS internals and computer network architectures.', TRUE),
(4, 'Ananya Iyer', 'ananya@studysync.edu', '$2a$10$eSluIiiL/O3Qhts28/MxAOR0HeDFkOxkrDgatrVf6bicqyKm/wHVe', 'Data Science', 'STUDENT', 'Machine learning practitioner exploring statistical analysis and mathematical modeling.', TRUE),
(5, 'Vikram Malhotra', 'vikram@studysync.edu', '$2a$10$eSluIiiL/O3Qhts28/MxAOR0HeDFkOxkrDgatrVf6bicqyKm/wHVe', 'AI & ML', 'STUDENT', 'Math enthusiast looking to collaborate on modern web frontends and UI design.', TRUE),
(6, 'Sneha Reddy', 'sneha@studysync.edu', '$2a$10$eSluIiiL/O3Qhts28/MxAOR0HeDFkOxkrDgatrVf6bicqyKm/wHVe', 'Software Engineering', 'STUDENT', 'Full-stack web developer experienced with modern HTML, CSS, and Java servlets.', TRUE),
(7, 'System Administrator', 'admin@studysync.edu', '$2a$10$d6sQ9G013O.s9D40cS/2P.Uau4tw7g4mgxdePR8qSoewA5zz6o8Da', 'Computer Science Dept', 'ADMIN', 'Academic system administrator overseeing content moderation and student accounts.', TRUE);

-- ------------------------------------------------------------------------------
-- 2. SKILLS (Master Dictionary)
-- ------------------------------------------------------------------------------
INSERT INTO skills (skill_id, skill_name) VALUES
(1, 'Java'),
(2, 'Python'),
(3, 'DBMS'),
(4, 'DSA'),
(5, 'Operating Systems'),
(6, 'Computer Networks'),
(7, 'Web Development'),
(8, 'Machine Learning'),
(9, 'Mathematics');

-- ------------------------------------------------------------------------------
-- 3. STUDENT_SKILLS (Mutual match scenario between Aarav and Diya + realistic network)
-- Aarav (1): Knows Python, DBMS | Needs Java
-- Diya (2):  Knows Java, DSA    | Needs DBMS
-- --> MUTUAL MATCH: Aarav needs Java (Diya knows), Diya needs DBMS (Aarav knows)
-- ------------------------------------------------------------------------------
INSERT INTO student_skills (user_id, skill_id, skill_type) VALUES
-- Aarav Sharma (CS)
(1, 2, 'KNOWN'),  -- Python
(1, 3, 'KNOWN'),  -- DBMS
(1, 1, 'NEEDED'), -- Java

-- Diya Patel (IT)
(2, 1, 'KNOWN'),  -- Java
(2, 4, 'KNOWN'),  -- DSA
(2, 3, 'NEEDED'), -- DBMS

-- Rohan Verma (Electronics)
(3, 5, 'KNOWN'),  -- Operating Systems
(3, 6, 'KNOWN'),  -- Computer Networks
(3, 2, 'NEEDED'), -- Python

-- Ananya Iyer (Data Science)
(4, 8, 'KNOWN'),  -- Machine Learning
(4, 2, 'KNOWN'),  -- Python
(4, 4, 'NEEDED'), -- DSA

-- Vikram Malhotra (AI & ML)
(5, 9, 'KNOWN'),  -- Mathematics
(5, 8, 'KNOWN'),  -- Machine Learning
(5, 7, 'NEEDED'), -- Web Development

-- Sneha Reddy (Software Engineering)
(6, 7, 'KNOWN'),  -- Web Development
(6, 1, 'KNOWN'),  -- Java
(6, 5, 'NEEDED'); -- Operating Systems

-- ------------------------------------------------------------------------------
-- 4. NOTES (Shared lecture materials with existing files)
-- ------------------------------------------------------------------------------
INSERT INTO notes (note_id, user_id, title, subject, module, original_filename, stored_filename, file_path, file_size) VALUES
(1, 2, 'Java OOP & Design Principles Summary', 'Java', 'Module 3', 'sample-java-notes.pdf', 'sample-java-notes.pdf', 'sample-java-notes.pdf', 624),
(2, 1, 'Relational Database Design & Normalization', 'DBMS', 'Module 2', 'sample-dbms-notes.pdf', 'sample-dbms-notes.pdf', 'sample-dbms-notes.pdf', 648),
(3, 2, 'Data Structures & Algorithms Cheat Sheet', 'DSA', 'Module 1', 'sample-dsa-notes.txt', 'sample-dsa-notes.txt', 'sample-dsa-notes.txt', 580),
(4, 3, 'Operating System Process Synchronization', 'Operating Systems', 'Module 4', 'sample-java-notes.pdf', 'sample-java-notes.pdf', 'sample-java-notes.pdf', 624),
(5, 4, 'Machine Learning Supervised Algorithms', 'Machine Learning', 'Module 2', 'sample-dbms-notes.pdf', 'sample-dbms-notes.pdf', 'sample-dbms-notes.pdf', 648);

-- ------------------------------------------------------------------------------
-- 5. STUDY_REQUESTS (Covering PENDING, ACCEPTED, and REJECTED states)
-- ------------------------------------------------------------------------------
INSERT INTO study_requests (request_id, sender_id, receiver_id, subject, status, created_at) VALUES
-- Pending request: Rohan asks Diya for Java tutoring
(1, 3, 2, 'Java', 'PENDING', NOW() - INTERVAL 2 DAY),
-- Accepted request: Aarav asks Diya for Java tutoring (mutual match pair)
(2, 1, 2, 'Java', 'ACCEPTED', NOW() - INTERVAL 5 DAY),
-- Rejected request: Vikram asks Sneha for Web Development assistance
(3, 5, 6, 'Web Development', 'REJECTED', NOW() - INTERVAL 7 DAY),
-- Completed request history: Diya asks Aarav for DBMS
(4, 2, 1, 'DBMS', 'ACCEPTED', NOW() - INTERVAL 10 DAY);

-- ------------------------------------------------------------------------------
-- 6. STUDY_SESSIONS (SCHEDULED, COMPLETED, CANCELLED)
-- ------------------------------------------------------------------------------
INSERT INTO study_sessions (session_id, request_id, subject, session_date, session_time, status) VALUES
-- 1. SCHEDULED session (Upcoming session from request #2 between Aarav and Diya)
(1, 2, 'Java', CURDATE() + INTERVAL 2 DAY, '15:30:00', 'SCHEDULED'),
-- 2. COMPLETED session (Historical completed session between Diya and Aarav)
(2, 4, 'DBMS', CURDATE() - INTERVAL 3 DAY, '14:00:00', 'COMPLETED'),
-- 3. CANCELLED session (Previous cancelled session)
(3, 2, 'Java', CURDATE() - INTERVAL 6 DAY, '11:00:00', 'CANCELLED');

-- ------------------------------------------------------------------------------
-- 7. SESSION_PARTICIPANTS
-- ------------------------------------------------------------------------------
INSERT INTO session_participants (session_id, student_id) VALUES
(1, 1), -- Session 1: Aarav
(1, 2), -- Session 1: Diya
(2, 2), -- Session 2: Diya
(2, 1), -- Session 2: Aarav
(3, 1), -- Session 3: Aarav
(3, 2); -- Session 3: Diya

-- ------------------------------------------------------------------------------
-- 8. RATINGS (Reviews exchanged for Completed Session #2)
-- ------------------------------------------------------------------------------
INSERT INTO ratings (rating_id, session_id, reviewer_id, reviewee_id, rating_value, feedback, created_at) VALUES
-- Aarav rates Diya
(1, 2, 1, 2, 5, 'Diya was fantastic at clarifying Java Collections and memory management concepts!', NOW() - INTERVAL 3 DAY),
-- Diya rates Aarav
(2, 2, 2, 1, 5, 'Aarav gave crystal-clear explanations of database normalization and SQL indexing.', NOW() - INTERVAL 3 DAY);
