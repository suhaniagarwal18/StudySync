# 📚 StudySync — Peer Study Buddy & Notes Exchange System

[![Java 17](https://img.shields.io/badge/Java-17%20LTS-orange.svg)](https://adoptium.net/)
[![Jakarta EE 10](https://img.shields.io/badge/Jakarta%20EE-10-blue.svg)](https://jakarta.ee/)
[![Apache Tomcat](https://img.shields.io/badge/Tomcat-10.1-yellowgreen.svg)](https://tomcat.apache.org/)
[![MySQL 8](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Maven 3.9](https://img.shields.io/badge/Maven-3.9-red.svg)](https://maven.apache.org/)
[![JUnit 5](https://img.shields.io/badge/JUnit-5.10-green.svg)](https://junit.org/junit5/)
[![Test Coverage](https://img.shields.io/badge/Tests-37%20Passed-brightgreen.svg)]()
[![E2E Verification](https://img.shields.io/badge/E2E%20HTTP-19%20Passed-brightgreen.svg)]()

> A robust, demo-ready university web application built strictly with **Core Java 17, Jakarta Servlets 6.0, JSP 3.1 + JSTL 3.0, JDBC, and MySQL 8**.  
> Engineered for 3rd-year B.Tech Computer Science viva evaluation to demonstrate mastery over OOP, Collections, Exception Handling, File I/O, Serialization, Transactions, and Web Security.

---

## 🏛️ System Architecture

StudySync follows an industry-standard **Model-View-Controller (MVC)** layered architecture with separation of concerns:

```
                            +---------------------------------+
                            |       Browser / HTTP Client     |
                            +---------------------------------+
                                             |
                                  [HTTP GET / POST + CSRF]
                                             v
                            +---------------------------------+
                            |         Servlet Filter Chain    |
                            |   1. EncodingFilter (UTF-8)     |
                            |   2. AuthFilter (Session Check) |
                            |   3. AdminFilter (RBAC)         |
                            |   4. CsrfFilter (Token Verify)  |
                            +---------------------------------+
                                             |
                                             v
                            +---------------------------------+
                            |       Controllers (Servlets)    |
                            |   Login, Profile, Buddy, Notes, |
                            |   Requests, Sessions, Admin     |
                            +---------------------------------+
                                             |
                                             v
                            +---------------------------------+
                            |         Service Layer           |
                            |  MatchingService, AuthService,  |
                            |  ProfileService, NoteService... |
                            +---------------------------------+
                                   /                   \
                                  v                     v
            +--------------------------+   +--------------------------+
            |  DAO Layer (JDBC)        |   | Filesystem & I/O Storage |
            |  PreparedStatements      |   | Magic Bytes Verification |
            |  DBCP2 Connection Pool   |   | Path Traversal Defense   |
            +--------------------------+   +--------------------------+
                         |                              |
                         v                              v
            +--------------------------+   +--------------------------+
            | MySQL 8 Database Server  |   | uploads/ Directory       |
            | (InnoDB, Foreign Keys)   |   | (PDFs, Serialized Cache) |
            +--------------------------+   +--------------------------+
```

---

## 🔑 Demonstration Credentials

The application is pre-seeded with realistic academic accounts across various university engineering departments. All passwords use authentic **BCrypt** hashes ($2^{10}$ work factor):

| Role | Name | Department | Email | Password |
|---|---|---|---|---|
| **Admin** | System Administrator | CS Department | `admin@studysync.edu` | `Admin@123` |
| **Student** | Aarav Sharma | Computer Science | `aarav@studysync.edu` | `Student@123` |
| **Student** | Diya Patel | Information Technology | `diya@studysync.edu` | `Student@123` |
| **Student** | Rohan Verma | Electronics | `rohan@studysync.edu` | `Student@123` |
| **Student** | Ananya Iyer | Data Science | `ananya@studysync.edu` | `Student@123` |
| **Student** | Vikram Malhotra | AI & ML | `vikram@studysync.edu` | `Student@123` |
| **Student** | Sneha Reddy | Software Engineering | `sneha@studysync.edu` | `Student@123` |

> 💡 **Notice the Mutual Match Scenario:**  
> - **Aarav** knows *Python, DBMS* and needs *Java*.  
> - **Diya** knows *Java, DSA* and needs *DBMS*.  
> Navigating to **Find Buddies** under Aarav immediately calculates a **100% Mutual Match** with Diya!

---

## ✨ Key Features & Capabilities

### 1. Peer Study Buddy Matching
- **Set Intersection Algorithm:** Evaluates reciprocal skill sets between students in $O(N \cdot K)$ time.
- **Scoring Engine:** Distinguishes between Mutual Matches (100%), Direct Matches (75%), and incorporates a 10% department tie-breaker bonus.
- **Natural Ordering:** Implements `Comparable<MatchResult>` to sort matches by score descending, rating descending, and name ascending.

### 2. Study Requests & State Machine
- Lifecycle states: `PENDING` $\to$ `ACCEPTED` / `REJECTED` / `CANCELLED`.
- Invariant defenses: Students cannot send requests to themselves, cannot request subjects a peer hasn't listed under known skills, and cannot create duplicate invitations.

### 3. Study Sessions & Multi-Table JDBC Transactions
- Converting an accepted request into a study session uses **ACID transactions** (`conn.setAutoCommit(false)`), creating session rows, linking participants, and updating request states atomically with automatic rollback on error.

### 4. Notes Exchange & Secure File Streaming
- **Magic Byte Verification:** Inspects the first 8–16 binary bytes of uploads to confirm authentic PDF, JPEG, or PNG formats, rejecting spoofed executable files.
- **Path Traversal Defense:** Sanitizes filenames, stores files under UUID identifiers, and validates paths using `path.normalize().startsWith(uploadRoot)`.
- **Chunked Streaming:** Downloads are delivered through buffered streams (`16 KB` blocks) with accurate MIME headers.

### 5. Peer Ratings & Reviews
- Students submit 1–5 star ratings and academic feedback upon session completion.
- Star distribution aggregates are dynamically computed via SQL joins and displayed on student dashboards.

### 6. Administrative Management & Moderation
- Dedicated console for system administrators.
- Live platform statistics, student account activation/deactivation toggles, and note moderation/deletion.

### 7. Production-Grade Web Security
- **Strict Jakarta EE 10 / Servlet 6.0 Stack:** No legacy `javax.*` packages.
- **Zero Scriptlets:** 100% of JSPs use JSTL 3.0 (`<c:out>`) and Jakarta Taglibs for automatic XSS escaping.
- **Synchronizer Token CSRF Defense:** Enforced on all `POST`, `PUT`, and `DELETE` requests via `CsrfFilter` using constant-time hash comparisons.
- **Session Fixation Defense:** Invocations of `req.changeSessionId()` upon login.
- **Post-Redirect-Get (PRG):** Prevents duplicate form resubmissions upon browser refresh.

---

## 🛠️ Prerequisites & Setup Guide

### 1. Environment Requirements
- **Java Development Kit:** OpenJDK 17 or higher
- **Build Tool:** Apache Maven 3.8+
- **Application Server:** Apache Tomcat 10.1+ (Servlet 6.0 compliant)
- **Database:** MySQL 8.0+ or MariaDB 10.11+

### 2. Database Initialization
Start your MySQL server on port `3306` and execute the schema and sample data scripts:

```bash
# Initialize schema and tables
mysql -u root -p < database/schema.sql

# Seed academic demonstration records
mysql -u root -p studysync < database/sample_data.sql
```

### 3. Application Configuration
Verify or edit `src/main/resources/application.properties` to match your local database credentials:

```properties
db.url=jdbc:mysql://localhost:3306/studysync?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=root

db.pool.maxTotal=10
db.pool.maxIdle=5
db.pool.minIdle=1
db.pool.maxWaitMillis=5000

upload.dir=studysync-uploads
file.maxSize=10485760
```

---

## 🚀 Build, Test & Deployment

### Run Automated Unit & Integration Tests
```bash
mvn clean test
```
*Expected: 37 tests run, 0 failures, 0 errors in ~5 seconds.*

### Build Deployable WAR
```bash
mvn clean package
```
*Output: `target/StudySync.war`*

### Deploy to Apache Tomcat 10.1
1. Copy `target/StudySync.war` to your Tomcat `webapps/` folder:
   ```bash
   cp target/StudySync.war $CATALINA_HOME/webapps/StudySync.war
   ```
2. Start Tomcat:
   ```bash
   # Windows
   %CATALINA_HOME%\bin\startup.bat

   # Linux / macOS
   $CATALINA_HOME/bin/startup.sh
   ```
3. Open your browser and navigate to:  
   👉 **`http://localhost:8080/StudySync/`**

### Run Automated End-to-End HTTP Test Suite
To execute the automated 19-scenario HTTP verification suite:
```powershell
powershell -ExecutionPolicy Bypass -File scratch/test_e2e.ps1
```
*Expected: 19 / 19 scenarios passed.*

---

## 📂 Project Directory Structure

```
StudySync/
├── database/
│   ├── schema.sql              # Database DDL (Users, Skills, Notes, Requests, Sessions)
│   ├── sample_data.sql         # Seed data with BCrypt hashes (1 Admin, 6 Students)
│   └── schema_test.sql         # Isolated schema for integration testing
├── docs/
│   ├── JAVA_CONCEPTS.md        # Comprehensive Viva Voce Guide & 15 Q&As
│   └── TESTING.md              # Test matrix (37 JUnit tests + 19 HTTP scenarios)
├── scratch/
│   └── test_e2e.ps1            # Automated PowerShell HTTP verification suite
├── src/
│   ├── main/
│   │   ├── java/com/studysync/
│   │   │   ├── dao/            # Data Access Objects (UserDAO, NoteDAO, SkillDAO...)
│   │   │   ├── exception/      # Custom hierarchy (ValidationException, AuthException...)
│   │   │   ├── filter/         # EncodingFilter, AuthFilter, AdminFilter, CsrfFilter
│   │   │   ├── listener/       # AppInitListener (Directory and resource bootstrap)
│   │   │   ├── model/          # Domain entities (User, Student, Admin, Note, Session...)
│   │   │   ├── service/        # Business logic (MatchingService, AuthService, Notes...)
│   │   │   ├── servlet/        # MVC Controllers (Login, Buddy, Request, Session, Admin...)
│   │   │   └── util/           # DBUtil, PasswordUtil, CsrfUtil, FlashUtil, FileStorageUtil
│   │   ├── resources/
│   │   │   ├── application.properties  # Database & storage properties
│   │   │   └── sample-notes/   # Seed academic documents (PDF, TXT)
│   │   └── webapp/
│   │       ├── css/style.css   # Modern, responsive university CSS
│   │       ├── js/app.js       # Client validation, auto-dismiss alerts, confirmation modals
│   │       ├── index.jsp       # Landing page
│   │       └── WEB-INF/
│   │           ├── web.xml     # Jakarta Servlet 6.0 deployment descriptor
│   │           └── views/      # Protected JSPs (common, admin, student views, error pages)
│   └── test/
│       ├── java/com/studysync/ # 8 JUnit test classes covering all system components
│       └── resources/          # Test configuration pointing to studysync_test
└── pom.xml                     # Maven POM (Java 17, Servlet 6.0, JSTL 3.0, DBCP2, jBCrypt)
```

---

## 🎓 Academic Viva Cheat Sheet

When demonstrating this project to university faculty, highlight these key topics:

1. **Why Abstract Class for `User`?**  
   Both `Student` and `Admin` share state (`userId`, `name`, `email`, `passwordHash`) and common authentication logic, while delegating routing polymorphically via `getDashboardPath()`.
2. **How is the Matching Algorithm Implemented?**  
   Using Java Collections set operations (`Set.retainAll()` / set intersection) between `A.neededSkills` and `B.knownSkills`, sorted using `Comparable<MatchResult>`.
3. **How is SQL Injection Prevented?**  
   Every single SQL query uses `PreparedStatement` with parameterized placeholders (`?`). No SQL strings are ever concatenated.
4. **How are Multi-Table Operations Kept Atomic?**  
   In `StudySessionDAO`, `conn.setAutoCommit(false)` coordinates multiple inserts and updates, committing only on success and executing `conn.rollback()` on failure.
5. **How is File Upload Security Handled?**  
   Three layers: Magic Byte inspection of binary headers, UUID renaming to prevent overwrite attacks, and path normalization to block directory traversal attacks (`../../`).

---

## ⚖️ License & Attribution
Developed for academic coursework in **Java Programming (Semester 5 B.Tech Computer Science & Engineering)**.  
Built strictly conforming to Java 17, Jakarta EE 10, and Tomcat 10.1 standards.
