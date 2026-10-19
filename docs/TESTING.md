# StudySync — Comprehensive Testing & Verification Report

**Project:** StudySync – Peer Study Buddy & Notes Exchange System  
**Test Execution Environment:**  
- **JDK:** OpenJDK 17.0.20.1  
- **Test Framework:** JUnit 5 (Jupiter 5.10.2) + AssertJ 3.25.3  
- **Web Server:** Apache Tomcat 10.1.20 (HTTP port 8080)  
- **Database:** MariaDB 10.11.7 (MySQL 8 wire-compatible, port 3306)  
- **Build Tool:** Apache Maven 3.9.6  

---

## 1. Test Strategy Overview

StudySync uses a multi-layered testing strategy to guarantee security, data integrity, and algorithm accuracy:
1. **Unit Tests (JUnit 5):** Focused on isolated business logic without network or database dependencies (e.g., set-intersection matching algorithm, BCrypt hashing, input validation rules, file path traversal sanitization, and binary serialization).
2. **Database Integration Tests (JUnit 5 + JDBC):** Connects to the isolated `studysync_test` database schema, verifying connection pooling, CRUD operations, transactions, and foreign key cascades.
3. **End-to-End HTTP Scenarios (Automated PowerShell Suite):** Executes realistic HTTP sessions against the live running application deployed on Apache Tomcat 10.1, verifying session handling, cookie management, filter pipelines, CSRF protection, RBAC defenses, and file downloads.

```
       +-------------------------------------------------------+
       |         End-to-End HTTP Verification (19 Scenarios)    |
       |     (Tomcat 10.1 + Filters + Servlets + JSPs + JDBC)   |
       +-------------------------------------------------------+
                                  |
       +-------------------------------------------------------+
       |           Database Integration Tests (1 Suite)        |
       |         (MariaDB/MySQL + Connection Pool + DAOs)      |
       +-------------------------------------------------------+
                                  |
       +-------------------------------------------------------+
       |          Automated Unit Test Suites (36 Tests)        |
       |      (Matching, Security, Validation, I/O, SerDe)     |
       +-------------------------------------------------------+
```

---

## 2. Automated JUnit 5 Test Results (37 / 37 Passed)

Executed via `mvn test`. Build time: **5.82 seconds**. Failures: **0**, Errors: **0**, Skipped: **0**.

| # | Test Suite Class | Test Method | Test Objective / Verification | Result |
|---|---|---|---|---|
| 1 | `PasswordUtilTest` | `testHashAndVerify_Success()` | Verifies that plaintext password produces valid BCrypt hash and verifies successfully. | **PASS** |
| 2 | `PasswordUtilTest` | `testVerify_WrongPassword()` | Verifies that incorrect passwords return `false`. | **PASS** |
| 3 | `PasswordUtilTest` | `testNullHandling()` | Verifies that null or empty passwords/hashes return `false` without throwing exceptions. | **PASS** |
| 4 | `ValidationUtilTest` | `testValidateEmail_Valid()` | Validates valid institutional emails (`aarav@studysync.edu`). | **PASS** |
| 5 | `ValidationUtilTest` | `testValidateEmail_Invalid()` | Rejects malformed emails missing `@` or domain. | **PASS** |
| 6 | `ValidationUtilTest` | `testValidatePassword_Strong()` | Accepts passwords with $\ge 8$ chars, uppercase, lowercase, digit, and symbol. | **PASS** |
| 7 | `ValidationUtilTest` | `testValidatePassword_Weak()` | Rejects passwords that are too short or lack required complexity. | **PASS** |
| 8 | `ValidationUtilTest` | `testValidateRegistration_Success()` | Accepts well-formed registration parameters. | **PASS** |
| 9 | `ValidationUtilTest` | `testNormalizeSkillName()` | Strips leading/trailing spaces and capitalizes correctly (`"  java "` $\to$ `"Java"`). | **PASS** |
| 10 | `ValidationUtilTest` | `testToComparisonKey()` | Normalizes strings for case-insensitive matching (`"C++"` $\to$ `"c++"`). | **PASS** |
| 11 | `ValidationUtilTest` | `testSanitizeHtml()` | Escapes `<script>` and HTML injection tokens to safe HTML entities. | **PASS** |
| 12 | `MatchingServiceTest` | `testMutualMatch_Returns100Percent()` | Verifies reciprocal need/know between two students yields 100% compatibility score. | **PASS** |
| 13 | `MatchingServiceTest` | `testDirectMatch_Returns75Percent()` | Verifies one-way skill sharing yields 75% compatibility score. | **PASS** |
| 14 | `MatchingServiceTest` | `testNoMatch_ReturnsZero()` | Verifies disjoint skill sets produce 0% score and are excluded from results. | **PASS** |
| 15 | `MatchingServiceTest` | `testDepartmentBonus()` | Verifies students from the same department receive the 10% tie-breaker bonus. | **PASS** |
| 16 | `StudyRequestTest` | `testInitialStateIsPending()` | Verifies a newly instantiated `StudyRequest` defaults to `RequestStatus.PENDING`. | **PASS** |
| 17 | `StudyRequestTest` | `testAcceptPendingRequest()` | Verifies state transition from `PENDING` $\to$ `ACCEPTED`. | **PASS** |
| 18 | `StudyRequestTest` | `testRejectPendingRequest()` | Verifies state transition from `PENDING` $\to$ `REJECTED`. | **PASS** |
| 19 | `StudyRequestTest` | `testCancelPendingRequest()` | Verifies state transition from `PENDING` $\to$ `CANCELLED`. | **PASS** |
| 20 | `StudyRequestTest` | `testCannotAcceptAlreadyAcceptedRequest()` | Enforces state machine invariant: `ACCEPTED` request cannot be re-accepted. | **PASS** |
| 21 | `StudyRequestTest` | `testCannotRejectAcceptedRequest()` | Enforces state machine invariant: `ACCEPTED` request cannot be rejected. | **PASS** |
| 22 | `StudyRequestTest` | `testSelfRequestDisallowed()` | Enforces business rule: student cannot send a study request to themselves. | **PASS** |
| 23 | `StudySessionTest` | `testSessionLifecycle_ScheduleToComplete()` | Verifies transition `SCHEDULED` $\to$ `COMPLETED` records completion timestamp. | **PASS** |
| 24 | `StudySessionTest` | `testSessionLifecycle_ScheduleToCancel()` | Verifies transition `SCHEDULED` $\to$ `CANCELLED`. | **PASS** |
| 25 | `StudySessionTest` | `testCannotCompleteCancelledSession()` | Invariant defense: cancelled session cannot be marked completed. | **PASS** |
| 26 | `StudySessionTest` | `testAddParticipant()` | Adds student to participant list and checks membership. | **PASS** |
| 27 | `StudySessionTest` | `testCannotAddDuplicateParticipant()` | Prevents adding the same student ID twice to a session. | **PASS** |
| 28 | `FileStorageUtilTest` | `testSanitizeFilename_PathTraversalDefense()` | Strips `../../etc/passwd` down to harmless basename `passwd`. | **PASS** |
| 29 | `FileStorageUtilTest` | `testSanitizeFilename_IllegalChars()` | Replaces illegal filesystem characters (`:*?"<>|`) with underscores. | **PASS** |
| 30 | `FileStorageUtilTest` | `testVerifyMagicBytes_PdfValid()` | Confirms authentic `%PDF-` header matches PDF whitelist. | **PASS** |
| 31 | `FileStorageUtilTest` | `testVerifyMagicBytes_PdfSpoofed()` | Rejects executable file pretending to be PDF (`.pdf` extension with `MZ` header). | **PASS** |
| 32 | `FileStorageUtilTest` | `testVerifyMagicBytes_ImageValid()` | Confirms PNG (`\x89PNG`) and JPEG (`\xFF\xD8\xFF`) signatures. | **PASS** |
| 33 | `FileStorageUtilTest` | `testIsAllowedExtension()` | Validates whitelist enforcement (`pdf`, `txt`, `doc`, `docx`, `png`, `jpg`). | **PASS** |
| 34 | `FileStorageUtilTest` | `testResolveSecurely_TraversalBlocked()` | Verifies `resolveSecurely` throws `FileStorageException` on sandbox escape. | **PASS** |
| 35 | `SerializationTest` | `testSerializeAndDeserializeSummary()` | Verifies `StudySessionSummary` serializes to byte stream and restores with complete fidelity. | **PASS** |
| 36 | `SerializationTest` | `testTransientFieldsNotSerialized()` | Verifies `transient` fields are null upon deserialization and safely recomputed. | **PASS** |
| 37 | `DatabaseIntegrationTest` | `testCompleteUserAndSkillLifecycle()` | Live integration test against `studysync_test`: inserts student, assigns known/needed skills, queries by email, updates bio, and cleans up. | **PASS** |

---

## 3. Automated End-to-End HTTP Verification Matrix (19 / 19 Passed)

Executed against live Tomcat 10.1 (`http://localhost:8080/StudySync`) via PowerShell test runner (`scratch/test_e2e.ps1`).  
All 19 scenarios passed with zero failures.

| ID | Test Scenario | HTTP Route & Method | Inputs / Action | Expected Outcome | Actual Outcome | Status |
|---|---|---|---|---|---|---|
| **SC-01** | Anonymous Landing Page | `GET /` | Unauthenticated browser request | Status 200, welcome banner and navigation rendered | Status 200, 4418 bytes rendered | **PASS** |
| **SC-02** | Unauthenticated Protected Access | `GET /dashboard` | No session cookie | 302 Redirect to `/login` | 302 Redirect to `/StudySync/login` | **PASS** |
| **SC-03** | CSRF Token Generation | `GET /login` | Initial visit | Hidden input with cryptographically secure token | Token extracted (`ovuFLpK07u...`) | **PASS** |
| **SC-04** | Student Authentication | `POST /login` | `aarav@studysync.edu` / `Student@123` + CSRF | 302 Redirect to `/dashboard`, session created | Authenticated, redirected to `/dashboard` | **PASS** |
| **SC-05** | Student Dashboard View | `GET /dashboard` | Authenticated session | Displays "Welcome back, Aarav Sharma!", department, skills | Status 200, department & skills profile displayed | **PASS** |
| **SC-06** | Dynamic Skill Addition | `POST /skills` | `action=add`, `skillName=Deep Learning`, `skillType=NEEDED` | PRG redirect to `/profile` with success flash message | Added 'Deep Learning' (NEEDED) via PRG | **PASS** |
| **SC-07** | Study Buddy Matching | `GET /buddies` | Authenticated session | Displays Diya Patel with reciprocal compatibility score | Found Diya Patel with compatibility score % | **PASS** |
| **SC-08** | Send Study Request | `POST /requests` | `action=send`, `receiverId=2`, `subject=Java` | Invitation created in `PENDING` state, success flash | Request submitted, flash message displayed | **PASS** |
| **SC-09** | Study Requests Dashboard | `GET /requests` | Authenticated session | Lists incoming & outgoing study buddy invitations | Status 200, rendered pending/sent tabs | **PASS** |
| **SC-10** | Study Sessions List | `GET /sessions` | Authenticated session | Lists scheduled and completed peer sessions | Status 200, sessions list rendered | **PASS** |
| **SC-11** | Notes Repository List | `GET /notes` | Authenticated session | Displays uploaded academic notes repository | Status 200, sample notes rendered | **PASS** |
| **SC-12** | Secure Note Download | `GET /notes/download?id=1` | Authenticated session | Binary stream of PDF with `application/pdf` header | Status 200, Content-Type `application/pdf`, 581 bytes | **PASS** |
| **SC-13** | RBAC Route Protection | `GET /admin/dashboard` | Student session (Aarav) | HTTP 403 Forbidden | Status 403 Forbidden returned | **PASS** |
| **SC-14** | Admin Authentication | `POST /login` | `admin@studysync.edu` / `Admin@123` + CSRF | 302 Redirect to `/admin/dashboard` | Authenticated, redirected to `/admin/dashboard` | **PASS** |
| **SC-15** | Admin User Management | `GET /admin/users` | Admin session | Renders user table with active toggle actions | Status 200, all 7 users listed | **PASS** |
| **SC-16** | Admin Note Moderation | `GET /admin/notes` | Admin session | Renders all uploaded notes with delete controls | Status 200, notes moderation panel rendered | **PASS** |
| **SC-17** | CSRF Attack Defense | `POST /skills` | Missing `csrfToken` parameter | HTTP 403 Forbidden | Status 403 Forbidden rejected request | **PASS** |
| **SC-18** | Authentication Defense | `POST /login` | Incorrect password `WrongPassword999!` | Re-displays login page with generic error message | "Invalid email or password" displayed | **PASS** |
| **SC-19** | User Logout & Cleanup | `POST /logout` | Authenticated session + CSRF | Session invalidated, 302 Redirect to `/login` | Redirected to `/login` with logout flash | **PASS** |

---

## 4. Manual Verification & Demo Walkthrough Guide

Follow these steps during project demonstration or viva evaluation:

### Step 1: Open Application in Web Browser
- Navigate to: `http://localhost:8080/StudySync/`
- Observe the landing page with platform highlights and Call-to-Action buttons.

### Step 2: Student Login & Matching Demo
- Click **Log In** (top right) or go to `/login`.
- Enter:
  - **Email:** `aarav@studysync.edu`
  - **Password:** `Student@123`
- Click **Sign In**.
- Observe the redirect to the Student Dashboard (`/dashboard`) displaying:
  - Welcome greeting: *"Welcome back, Aarav Sharma!"*
  - Department badge: *Computer Science*
  - Metric cards: Pending Requests, Study Sessions, Shared Notes, Peer Rating.
  - Known skills: *Python, DBMS*. Needed skills: *Java*.

### Step 3: Test Study Buddy Matching Algorithm
- Click **Find Study Buddies** from the navigation bar (`/buddies`).
- Observe **Diya Patel (Information Technology)** appearing at the top with a **100% Mutual Match**:
  - *"Diya knows Java (which you need)"*
  - *"Diya needs DBMS (which you know)"*
- Click **Send Study Request** to send an invitation for *"Java"*.

### Step 4: Test Notes Download & Upload
- Click **Notes Exchange** (`/notes`).
- Notice the uploaded notes: *"Complete Java & OOP Master Guide"*, *"Relational Database Management Systems"*.
- Click **Download** next to Note #1. Observe the browser cleanly downloading the PDF file with original filename preserved.
- Click **Upload Note** (`/notes/upload`). Fill in title, select subject, attach a text or PDF file, and submit. Observe instant listing in the repository.

### Step 5: Test Admin Privileges & RBAC
- Click **Logout** to terminate Aarav's session.
- Log in as the System Administrator:
  - **Email:** `admin@studysync.edu`
  - **Password:** `Admin@123`
- Observe the redirect to the **Administrator Console** (`/admin/dashboard`).
- View total registered users, active students, notes count, and average platform rating.
- Navigate to **Manage Users** (`/admin/users`) to inspect student accounts or toggle their active status.
- Navigate to **Moderate Notes** (`/admin/notes`) to inspect or remove uploaded files.

---

## 5. Security & Invariant Audit Summary

| Security Control | Implementation Mechanism | Verification Method |
|---|---|---|
| **SQL Injection** | `PreparedStatement` with try-with-resources | Automated JDBC tests + code inspection |
| **Cross-Site Scripting (XSS)** | JSTL `<c:out value="..."/>` + `ValidationUtil.sanitizeHtml` | HTML injection payload verification |
| **CSRF** | Cryptographic token + `CsrfFilter` (constant-time comparison) | Automated HTTP Scenario 17 (Missing token $\to$ 403) |
| **Session Fixation** | `HttpServletRequest.changeSessionId()` on login | Session ID inspection before and after auth |
| **Path Traversal** | `FileStorageUtil.resolveSecurely()` + Path normalization | Unit test `testResolveSecurely_TraversalBlocked` |
| **Spoofed MIME Types** | Binary Magic Byte header verification | Unit test `testVerifyMagicBytes_PdfSpoofed` |
| **RBAC Enforcement** | `AuthFilter` and `AdminFilter` pipeline | Automated HTTP Scenario 13 (Student accessing admin $\to$ 403) |
| **Password Storage** | BCrypt adaptive hashing ($2^{10}$ work factor) | Verified with live database hashes |
