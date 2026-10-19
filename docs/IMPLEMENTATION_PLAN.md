# StudySync – Implementation Plan

**Project:** StudySync – Peer Study Buddy & Notes Exchange System  
**Stack:** Java 17 | Jakarta Servlets 6.0 | JSP 3.1 + JSTL 3.0 (Jakarta) | JDBC | MySQL 8 | Apache Tomcat 10.1 | Maven  
**Target WAR:** `StudySync.war` (Context path: `/StudySync`)

---

## 1. Architectural Layers & Package Structure

```
StudySync/
├── pom.xml
├── README.md
├── database/
│   ├── schema.sql
│   └── sample_data.sql
├── docs/
│   ├── IMPLEMENTATION_PLAN.md
│   ├── JAVA_CONCEPTS.md
│   └── TESTING.md
├── src/main/java/com/studysync/
│   ├── model/         # Domain models, enums, interfaces (OOP, Encapsulation, Polymorphism)
│   ├── dao/           # JDBC data access objects with PreparedStatements & transactions
│   ├── service/       # Business logic, validations, matching algorithm, session export
│   ├── servlet/       # Thin MVC controllers (Jakarta @WebServlet, PRG pattern)
│   ├── filter/        # Deterministic security filter pipeline (Encoding, Auth, Admin, CSRF)
│   ├── listener/      # App lifecycle listener (directory init, sample upload setup)
│   ├── util/          # Database, password, CSRF, flash, validation & file utilities
│   └── exception/     # Custom checked/unchecked exception hierarchy
├── src/main/resources/
│   ├── application.properties
│   └── sample-uploads/
├── src/main/webapp/
│   ├── index.jsp      # Public landing page
│   ├── css/style.css  # Academic portal theme, responsive CSS Grid / Flexbox
│   ├── js/app.js      # Minimal vanilla JS for confirm dialogs & mobile menu
│   ├── WEB-INF/
│   │   ├── web.xml    # Servlet 6.0 deployment descriptor, filter chain, security
│   │   └── views/     # Secured JSP views accessible only through servlets
│   │       ├── common/header.jspf, footer.jspf
│   │       ├── login.jsp, register.jsp, dashboard.jsp, profile.jsp
│   │       ├── findBuddy.jsp, studyRequests.jsp, studySessions.jsp, sessionDetails.jsp
│   │       ├── uploadNotes.jsp, viewNotes.jsp, ratings.jsp
│   │       ├── admin/ (adminDashboard.jsp, adminUsers.jsp, adminNotes.jsp)
│   │       └── error/ (403.jsp, 404.jsp, 500.jsp)
└── src/test/java/com/studysync/ # Comprehensive JUnit 5 unit & logic test suite
```

---

## 2. Database Schema (MySQL 8 / InnoDB / utf8mb4)

1. **`USERS`**: `user_id` (PK), `name`, `email` (UNIQUE), `password_hash`, `department`, `role` (`STUDENT` / `ADMIN`), `bio`, `is_active`, `created_at`.
2. **`SKILLS`**: `skill_id` (PK), `skill_name` (VARCHAR 80 UNIQUE, normalized case-insensitive).
3. **`STUDENT_SKILLS`**: `user_id` (FK), `skill_id` (FK), `skill_type` (`KNOWN` / `NEEDED`), Composite PK (`user_id`, `skill_id`, `skill_type`).
4. **`NOTES`**: `note_id` (PK), `user_id` (FK to USERS), `title`, `subject`, `module`, `original_filename`, `stored_filename` (UUID), `file_path`, `file_size`, `uploaded_at`.
5. **`STUDY_REQUESTS`**: `request_id` (PK), `sender_id` (FK), `receiver_id` (FK), `subject`, `status` (`PENDING` / `ACCEPTED` / `REJECTED`), `created_at`, CHECK (`sender_id <> receiver_id`).
6. **`STUDY_SESSIONS`**: `session_id` (PK), `request_id` (FK to STUDY_REQUESTS), `subject`, `session_date`, `session_time`, `status` (`SCHEDULED` / `COMPLETED` / `CANCELLED`).
7. **`SESSION_PARTICIPANTS`**: `session_id` (FK), `student_id` (FK), Composite PK (`session_id`, `student_id`).
8. **`RATINGS`**: `rating_id` (PK), `session_id` (FK), `reviewer_id` (FK), `reviewee_id` (FK), `rating_value` (1-5), `feedback`, `created_at`, UNIQUE (`session_id`, `reviewer_id`, `reviewee_id`), CHECK (`reviewer_id <> reviewee_id`).

---

## 3. Java Classes & Interfaces

### Model Layer
- `User` (abstract class): `userId`, `name`, `email`, `passwordHash`, `department`. Abstract methods: `getRole()`, `getDashboardPath()`, `getMenuItems()`. Concrete methods: `login()`, `logout()`, `updateProfile()`.
- `Student` extends `User`: `skillsKnown` (Set<String>), `skillsNeeded` (Set<String>), `bio`.
- `Admin` extends `User`: `adminId` (= `userId`).
- `Note` implements `Downloadable`: `noteId`, `title`, `subject`, `module`, `originalFilename`, `storedFilename`, `filePath`, `fileSize`, `uploadedAt`.
- `StudyRequest`: `requestId`, `sender` (Student), `receiver` (Student), `subject`, `status` (`RequestStatus`), `createdAt`.
- `StudySession`: `sessionId`, `requestId`, `subject`, `date`, `time`, `participants` (List<Student>), `status` (`SessionStatus`).
  *(Design note: StudySession and StudyRequest are linked via association, not inheritance, adhering to composition over inheritance and absence of an IS-A relationship)*.
- `Rating`: `ratingId`, `sessionId`, `reviewerId`, `revieweeId`, `ratingValue` (1-5), `feedback`, `createdAt`.
- `MatchResult`: `student`, `commonSkills` (Set<String>), `matchScore` (int), `averageRating` (double), `mutualMatch` (boolean).
- `StudySessionSummary` implements `Serializable`: Session export payload with `serialVersionUID` and non-sensitive attributes, plus a `transient` formatted cached string.
- Interfaces: `Searchable<T>`, `Downloadable`.
- Enums: `RequestStatus`, `SessionStatus`, `SkillType`.

### DAO Layer
- `UserDAO`: User CRUD, lookup by email, verify password, student skill hydration.
- `SkillDAO`: Canonical skill creation, user skill association, bulk student skill maps for matching.
- `NoteDAO`: Note metadata insertion, search by keyword/subject, user note listing, deletion.
- `StudyRequestDAO`: Request creation, sent/received queries, status transitions.
- `StudySessionDAO`: Transactional session creation with participants, queries, status updates.
- `RatingDAO`: Rating insertion, reviewee rating listing, average calculations.
- `StatsDAO`: Platform aggregated statistics for admin dashboard.

### Service Layer
- `AuthService`: Student registration, credential authentication, session bootstrap.
- `ProfileService`: Bio/department updates, skill additions and removals with validation.
- `MatchingService`: O(1) set-intersection buddy discovery with bonus weighting.
- `StudyRequestService`: Request lifecycle enforcement (anti-self, skill requirement, deduplication).
- `StudySessionService`: Scheduling from accepted requests, cancellation, completion.
- `NoteService`: File upload inspection, metadata recording, safe streaming, file deletion.
- `RatingService`: Post-completion rating verification, rating aggregation.
- `AdminService`: User activation toggling, note moderation, global metric aggregation.
- `SessionSummaryService`: Java Object Serialization & Deserialization round-trip.

### Filter & Security Pipeline (web.xml ordered)
1. `EncodingFilter`: Forces UTF-8 encoding across all requests and responses.
2. `AuthFilter`: Enforces authentication for protected paths; redirects guests to `/login`.
3. `AdminFilter`: Restricts `/admin/*` routes strictly to users with `role == ADMIN`.
4. `CsrfFilter`: Cryptographically secure token generation and constant-time validation on all POST requests.

### Servlets
- `/login`, `/register`, `/logout`
- `/dashboard`, `/profile`, `/skills`
- `/buddies`, `/requests`, `/sessions`
- `/notes`, `/notes/upload`, `/notes/download`
- `/ratings`
- `/admin/dashboard`, `/admin/users`, `/admin/notes`

---

## 4. Implementation Sequence

1. **Phase 0 & 1:** Check environment, setup `pom.xml`, configure dependencies and directory structure.
2. **Phase 2:** Write `database/schema.sql` and `database/sample_data.sql` with authentic BCrypt hashes.
3. **Phase 3:** Implement exceptions and utility classes (`DBUtil`, `AppConfig`, `PasswordUtil`, `ValidationUtil`, `CsrfUtil`, `FlashUtil`).
4. **Phase 4:** Build OOP model classes, interfaces, enums, demonstrating core concepts with Javadoc and viva comments.
5. **Phase 5:** Build DAO layer with JDBC try-with-resources, PreparedStatements, and transactions.
6. **Phase 6:** Implement Service layer business logic, constraints, and the buddy matching algorithm.
7. **Phase 7:** Implement security infrastructure: filters, CSRF token handling, and session rotation.
8. **Phase 8 & 9:** Implement Servlets and JSP views with modern, responsive academic CSS and JSTL tags.
9. **Phase 10:** Implement `FileStorageUtil` with magic-byte validation, UUID storage, and traversal prevention.
10. **Phase 11:** Implement `StudySessionSummary` Java Object Serialization demo.
11. **Phase 12:** Implement Admin portal features (user moderation, note deletion, metrics).
12. **Phase 13:** Configure `web.xml` (Jakarta EE 10 / Servlet 6.0), assemble WAR.
13. **Phase 14:** Write comprehensive JUnit 5 tests and compile/verify test suite.
14. **Phase 15 & 16:** Produce `README.md`, `JAVA_CONCEPTS.md`, `TESTING.md`, and complete final report.

---

## 5. Assumptions & Design Decisions

1. **Database Credentials:** Default MySQL configuration in `application.properties` uses `jdbc:mysql://localhost:3306/studysync?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` with user `root` and empty/configurable password, overrideable via environment variables `STUDYSYNC_DB_URL`, `STUDYSYNC_DB_USER`, `STUDYSYNC_DB_PASSWORD`.
2. **File Storage Directory:** Defaults to `${user.home}/studysync-uploads/notes` (and `/summaries`), completely outside the web application root to prevent arbitrary web execution. Overrideable via `STUDYSYNC_UPLOAD_DIR`.
3. **Demo Accounts:**
   - Admin: `admin@studysync.edu` / `Admin@123`
   - Students: `aarav@studysync.edu` / `Student@123`, `diya@studysync.edu` / `Student@123`, etc.
4. **Exception Handling Strategy:** `StudySyncException` is a checked exception representing recoverable business rule violations (rendered as user-friendly flash messages), while `DataAccessException` is an unchecked runtime wrapper for SQL/system exceptions (logging server-side, redirecting to friendly 500 error page).
5. **No Scriptlets:** Strictly 0 scriptlets (`<% %>`) in JSPs. All views use JSTL 3.0 (`jakarta.tags.core`, `jakarta.tags.functions`) and Expression Language with `<c:out>` escaping.
