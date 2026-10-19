# StudySync — Academic Java Concepts & Viva Preparation Guide

**Project:** StudySync – Peer Study Buddy & Notes Exchange System  
**Academic Level:** 3rd Year B.Tech Computer Science & Engineering  
**Technology Stack:** Java 17 | Jakarta Servlets 6.0 | JSP 3.1 + JSTL 3.0 | JDBC | MySQL 8 | Apache Tomcat 10.1 | Maven  

---

## Table of Contents
1. [Core Java & OOP Mapping](#1-core-java--oop-mapping)
   - [Encapsulation](#11-encapsulation)
   - [Inheritance](#12-inheritance)
   - [Polymorphism](#13-polymorphism)
   - [Abstraction & Interfaces](#14-abstraction--interfaces)
   - [Collections Framework](#15-collections-framework)
   - [Exception Handling Architecture](#16-exception-handling-architecture)
   - [File Handling & Stream I/O](#17-file-handling--stream-io)
   - [Object Serialization & Caching](#18-object-serialization--caching)
   - [JDBC & Database Persistence](#19-jdbc--database-persistence)
   - [Web Architecture, Servlets & Filters](#110-web-architecture-servlets--filters)
2. [15 Comprehensive Viva Voce Questions & Model Answers](#2-15-comprehensive-viva-voce-questions--model-answers)
3. [Design Trade-Offs & Architecture Rationales](#3-design-trade-offs--architecture-rationales)

---

## 1. Core Java & OOP Mapping

### 1.1 Encapsulation
- **Definition:** The bundling of state variables and behavioral methods within a single class, while restricting direct access through private fields and exposing controlled mutation via public accessor/mutator methods with internal invariant validation.
- **Where in StudySync:**
  - `com.studysync.model.User`: All entity fields (`userId`, `name`, `email`, `passwordHash`, `department`, `bio`, `active`) are declared `private`. Access is provided through explicit getters and setters.
  - `com.studysync.model.Student`: Keeps `knownSkills` and `neededSkills` as private `Set<String>` collections. Mutators like `addKnownSkill()` normalize strings and reject nulls or blanks.
  - `com.studysync.model.Note`: Fields like `downloadCount` are strictly encapsulated; `incrementDownloadCount()` encapsulates mutation logic rather than exposing a raw setter.
- **Why this pattern:** Prevents external servlets or DAO layers from putting entity models into corrupt or inconsistent states (e.g., negative download counts or unnormalized skill names).
- **Viva Pitfall:** *Q: Is generating getters and setters alone true encapsulation?*  
  *A:* No. Real encapsulation enforces business invariants (e.g., email validation, trim logic, state transitions) inside the methods, rather than simply acting as passthroughs.

---

### 1.2 Inheritance
- **Definition:** An OOP mechanism allowing a derived child class to acquire non-private attributes and methods from a parent class, facilitating code reuse and establishing an "IS-A" hierarchy.
- **Where in StudySync:**
  - `User` (abstract base class) $\leftarrow$ `Student` (subclass) and `Admin` (subclass).
  - `StudySyncException` (checked base exception) $\leftarrow$ `ValidationException`, `AuthenticationException`, `AuthorizationException`, `ResourceNotFoundException`, `FileStorageException`, `InvalidRequestException`.
- **Inherited Behavior:**
  - `Student` and `Admin` inherit user ID, name, email, credentials verification (`login()`), and timestamps from `User`.
  - `Student` introduces student-specific state: `knownSkills`, `neededSkills`, and academic rating aggregates.
  - `Admin` introduces administrative privilege flags and management capabilities.
- **Why this pattern:** Eliminates code duplication across user types while providing a clean polymorphic abstraction for authentication, session storage, and audit logging.

---

### 1.3 Polymorphism
- **Definition:** The ability of different classes to respond to the same method invocation in class-specific ways, either at compile-time (overloading) or runtime (dynamic method dispatch via overriding).
- **Where in StudySync:**
  - **Dynamic Polymorphism (Overriding):**
    - `User.getDashboardPath()`: `Student` returns `"/dashboard"`, while `Admin` returns `"/admin/dashboard"`.
    - `User.getRole()`: Polymorphically returns `"STUDENT"` or `"ADMIN"`.
    - `User.getNavItems()`: Returns a list of navigation bar links tailored to the user's operational role.
    - `LoginServlet`: Upon successful credential verification, invokes `resp.sendRedirect(req.getContextPath() + user.getDashboardPath());`. The servlet code treats the principal as `User` without inspecting concrete types.
  - **Static Polymorphism (Overloading):**
    - `MatchingService`: Overloaded matching methods: `findMatches(Student student, List<Student> candidates)` and `findMatches(int studentId, List<Student> candidates)`.
    - `StudySessionDAO`: Overloaded `listByStudent(int studentId)` and `listByStudentAndStatus(int studentId, SessionStatus status)`.
  - **Comparable Polymorphism:**
    - `MatchResult` implements `Comparable<MatchResult>`, overriding `compareTo(MatchResult other)` to sort matches by score descending, rating descending, and candidate name ascending.
- **Why this pattern:** Decouples controllers from concrete entity types, satisfying the Open/Closed Principle (OCP).

---

### 1.4 Abstraction & Interfaces
- **Definition:** Hiding implementation complexity and exposing only necessary conceptual contracts to callers.
- **Where in StudySync:**
  - `abstract class User`: Declares abstract contractual methods `getDashboardPath()`, `getRole()`, and `getNavItems()` that subclasses MUST implement.
  - `interface Searchable<T>`: Declares generic search contracts (`boolean matches(String query)`). Implemented by `Student` (matches on name, department, bio, skills) and `Note` (matches on title, subject, description, uploader).
  - `interface Downloadable`: Declares file retrieval contracts (`String getFilename()`, `String getMimeType()`, `byte[] getFileBytes()`). Implemented by `Note`.
- **Why this pattern:** Enables polymorphic search engines and file-streaming servlets to operate across diverse domain objects uniformly without coupling to concrete classes.

---

### 1.5 Collections Framework
- **Definition:** A unified architecture of interfaces and implementations in `java.util` for storing, organizing, and manipulating groups of objects in memory.
- **Where in StudySync:**
  - `Set<String>` (specifically `TreeSet<String>` with `String.CASE_INSENSITIVE_ORDER`): Used in `Student` for `knownSkills` and `neededSkills`. Eliminates duplicate skill entries and prevents casing discrepancies (`"Java"` vs `"java"`).
  - `List<MatchResult>` (`ArrayList`): Holds search results in `MatchingService`.
  - `Collections.sort(matches)`: Executes dual-pivot TimSort on the list using the natural order defined in `MatchResult.compareTo()`.
  - `Map<Integer, Double>`: Aggregates average peer ratings and review counts in `RatingDAO` and `StatsDAO`.
  - `Set<String>` operations: Used in `MatchingService` for set intersection:
    $$\text{Intersection} = \text{Student}_A.\text{neededSkills} \cap \text{Student}_B.\text{knownSkills}$$
- **Why this pattern:** Fast lookups ($O(1)$ for HashSet or $O(\log n)$ for TreeSet), inherent deduplication, and standard algorithms.

---

### 1.6 Exception Handling Architecture
- **Definition:** Structured mechanisms to detect, propagate, and recover from exceptional runtime error conditions while preserving application stability.
- **Where in StudySync:**
  - **Checked Exceptions (Extend `StudySyncException`):**
    - `ValidationException`: Raised when student registration, password criteria, or upload constraints fail.
    - `AuthenticationException`: Raised when credentials do not match or accounts are inactive.
    - `AuthorizationException`: Raised when a student attempts unauthorized mutations (e.g., accepting another peer's study request).
    - `ResourceNotFoundException`: Raised when requested IDs do not exist in the database.
    - `FileStorageException`: Raised on I/O failures, magic byte mismatches, or file traversal attempts.
    - *Why checked?* Forces calling servlets to handle business errors and provide human-friendly flash messages via `FlashUtil`.
  - **Unchecked Exceptions (Extend `RuntimeException`):**
    - `DataAccessException`: Wraps low-level `java.sql.SQLException`.
    - *Why unchecked?* Infrastructure failures (broken connection pool, DB down) cannot be recovered by servlets; wrapping them prevents polluting business interfaces with JDBC artifacts.
  - **Resource Management:**
    - Universal `try-with-resources` across all DAOs:
      ```java
      try (Connection conn = DBUtil.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)) {
          // Guaranteed safe close even on exceptions
      }
      ```

---

### 1.7 File Handling & Stream I/O
- **Definition:** Reading, writing, and validating persistent byte streams on the underlying filesystem using `java.nio.file` and `java.io`.
- **Where in StudySync:**
  - `FileStorageUtil.storeFile(Part filePart, String subfolder)`:
    - **Path Traversal Defense:** Strips directory prefixes via `Paths.get(submittedFilename).getFileName()`. Validates that the normalized resolved target path starts with the configured upload root.
    - **Magic Byte (MIME) Verification:** Does NOT trust user-submitted file extensions. Reads the initial 8–16 bytes:
      - PDF: `%PDF-` (`0x25 0x50 0x44 0x46 0x2D`)
      - JPEG: `0xFF 0xD8 0xFF`
      - PNG: `0x89 0x50 0x4E 0x47 0x0D 0x0A 0x1A 0x0A`
    - **UUID File Renaming:** Saves files as `<UUID>.<ext>` to avoid filesystem naming collisions.
  - `NoteDownloadServlet`:
    - Streams byte blocks using a 16 KB buffer (`byte[] buffer = new byte[16384]`) from `InputStream` directly into `HttpServletResponse.getOutputStream()`.
    - Sets `Content-Disposition: attachment; filename="..."` and proper `Content-Type`.
    - Automatically updates download counters.

---

### 1.8 Object Serialization & Caching
- **Definition:** The conversion of an in-memory Java object graph into a portable sequence of bytes (`java.io.Serializable`) for caching, storage, or transport across network boundaries.
- **Where in StudySync:**
  - `StudySessionSummary`: Implements `Serializable` with explicit `private static final long serialVersionUID = 1L;`.
  - Contains immutable snapshot data: session ID, subject, participant names, duration, and completion timestamp.
  - **`transient` Keyword:** Used on fields such as `cachedFormattedDisplay` to prevent redundant caching of computed presentation strings.
  - `SessionSummaryService`: Serializes session summary snapshots to disk cache (`.ser` files in `studysync-uploads/cache/`) and deserializes on cache hits using `ObjectInputStream` and `ObjectOutputStream`.

---

### 1.9 JDBC & Database Persistence
- **Definition:** The Java Database Connectivity API facilitating vendor-neutral communication between Java applications and relational database engines.
- **Where in StudySync:**
  - `DBUtil`: Initializes connection pooling via Apache Commons DBCP2 (`BasicDataSource`), configuring `maxTotal=10`, `maxIdle=5`, and `maxWaitMillis=5000`.
  - `PreparedStatement`: Used exclusively for 100% of SQL queries to protect against SQL Injection.
  - **Database Transactions:**
    - `StudySessionDAO.createSessionWithParticipants()`:
      ```java
      conn.setAutoCommit(false);
      // 1. Insert into study_sessions
      // 2. Insert rows into session_participants
      // 3. Update study_requests status to ACCEPTED
      conn.commit();
      // On exception: conn.rollback()
      ```
    - Demonstrates ACID compliance (Atomicity, Consistency, Isolation, Durability).

---

### 1.10 Web Architecture, Servlets & Filters
- **Definition:** The Jakarta Servlet API and Model-View-Controller (MVC) architectural pattern decoupling request handling, business logic, and presentation.
- **Where in StudySync:**
  - **Model:** Java Beans (`Student`, `Note`, `StudySession`, etc.) and DAOs.
  - **View:** JSP 3.1 + JSTL 3.0 located under protected `WEB-INF/views/` (inaccessible via direct browser URL).
  - **Controller:** Jakarta Servlets (`@WebServlet`) processing HTTP requests, invoking services, and dispatching views.
  - **Filter Pipeline (`jakarta.servlet.Filter`):**
    1. `EncodingFilter`: Sets UTF-8 encoding and security headers (`X-Content-Type-Options: nosniff`, `X-Frame-Options: SAMEORIGIN`).
    2. `AuthFilter`: Enforces authentication across protected URLs (`/dashboard`, `/profile`, `/buddies`, `/requests`, `/sessions`, `/notes/*`, `/ratings`).
    3. `AdminFilter`: Role-based access control protecting `/admin/*`.
    4. `CsrfFilter`: Validates CSRF tokens on state-changing HTTP methods (`POST`, `PUT`, `DELETE`).
  - **PRG Pattern (Post-Redirect-Get):** All form submissions redirect after mutation (`resp.sendRedirect()`) with flash messages stored in the session, preventing duplicate form resubmissions upon browser refresh.

---

## 2. 15 Comprehensive Viva Voce Questions & Model Answers

### Q1: What is the difference between an abstract class and an interface, and how did you choose between them in StudySync?
**Answer:**  
An abstract class can maintain instance state (private member variables) and partial concrete method implementations, whereas an interface primarily specifies a behavioral contract (or default methods).  
In StudySync, we used `abstract class User` because both `Student` and `Admin` share state (`userId`, `name`, `email`, `passwordHash`) and common behavior (`login()`, `logout()`), while enforcing child-specific behavior through abstract methods (`getDashboardPath()`, `getNavItems()`). In contrast, we used `Searchable<T>` and `Downloadable` as interfaces because they represent orthogonal capabilities that can be implemented by entirely unrelated domain entities (e.g., both a `Note` and a `Student` can be `Searchable`, but they do not share an inheritance hierarchy).

---

### Q2: Why did you use `PreparedStatement` instead of `Statement`? How does it prevent SQL injection?
**Answer:**  
`Statement` concatenates raw user strings directly into the SQL string, enabling malicious payloads to alter the query's AST (Abstract Syntax Tree).  
`PreparedStatement` pre-compiles the SQL template on the database server with parameter placeholders (`?`). When user values are bound via `ps.setString()` or `ps.setInt()`, the database driver treats them strictly as literal data values, never as executable SQL tokens, completely neutralizing SQL injection attacks. Additionally, pre-compilation allows the database engine to reuse execution plans, improving throughput.

---

### Q3: What is the Post-Redirect-Get (PRG) pattern and what problem does it solve in web development?
**Answer:**  
PRG is a web architectural design pattern where a controller, after processing a state-altering `POST` request, issues an HTTP `302/303 Redirect` instructing the client browser to make a `GET` request to a target view URL (e.g., redirecting to `/profile` after saving a skill).  
Without PRG, if a user refreshes their browser after submitting a form, the browser will prompt to resend the `POST` payload, leading to duplicate records (e.g., duplicate study requests or double uploads). In StudySync, flash messages are held in the HTTP session and cleared immediately upon consumption in the subsequent `GET` request.

---

### Q4: How does your buddy matching algorithm work? What is its time complexity?
**Answer:**  
The matching algorithm in `MatchingService` evaluates compatibility between student $A$ and student $B$ based on reciprocal skill sets:
1. It computes the intersection between student $A$'s needed skills and candidate $B$'s known skills:
   $$\text{A\_Needs\_B\_Knows} = \text{Skills}_A^{\text{needed}} \cap \text{Skills}_B^{\text{known}}$$
2. It computes the reverse intersection:
   $$\text{B\_Needs\_A\_Knows} = \text{Skills}_B^{\text{needed}} \cap \text{Skills}_A^{\text{known}}$$
3. If both intersections are non-empty, a **Mutual Match (100% score)** is awarded. If one direction matches, a **Direct Match (75% score)** is assigned. An extra 10% bonus is added if both students belong to the same academic department.
4. If $N$ is the number of candidates and $K$ is the average number of skills per student, the set intersection takes $O(K)$ using hash lookups. Sorting $M$ matches takes $O(M \log M)$ using dual-pivot TimSort. The overall time complexity is $O(N \cdot K + M \log M)$, which executes in milliseconds for university student populations.

---

### Q5: How do you prevent Path Traversal vulnerabilities during note file uploads and downloads?
**Answer:**  
Path traversal attacks exploit relative path characters (such as `../` or `..\`) to read or write files outside the intended storage directory.  
In StudySync, we implement a three-layer defense:
1. `FileStorageUtil.sanitizeOriginalFilename()` uses `Paths.get(filename).getFileName().toString()` to strip any directory traversal sequences.
2. The file is saved on disk under a cryptographically random `UUID` name, completely decoupling the stored filename from user input.
3. During download, `FileStorageUtil.resolveSecurely()` resolves the file path and calls `path.normalize().startsWith(configuredUploadRoot)`. If the normalized path leaves the upload sandbox, a `FileStorageException` is immediately thrown.

---

### Q6: Why did you check file magic bytes instead of trusting the file extension?
**Answer:**  
File extensions (like `.pdf` or `.png`) and HTTP `Content-Type` headers are user-controlled inputs that an attacker can easily spoof (e.g., uploading a malicious `.jsp` or `.exe` script renamed as `notes.pdf`).  
Magic bytes are the fixed signature sequences located at the exact beginning of a binary file format. For example, legitimate PDF files always start with `%PDF-` (`0x25 0x50 0x44 0x46`). `FileStorageUtil` inspects the initial byte stream directly using an input stream mark/reset or fixed buffer to verify authentic file signatures before persisting the file.

---

### Q7: What is the role of `serialVersionUID` in Java Serialization?
**Answer:**  
`serialVersionUID` is a unique 64-bit identifier used during object deserialization to ensure that the sender and receiver of a serialized object have loaded classes that are binary-compatible.  
If a class does not explicitly declare `serialVersionUID`, the Java runtime computes a default value based on class structure. If any field or method signature changes, the generated ID changes, causing an `InvalidClassException`. Declaring `private static final long serialVersionUID = 1L;` in `StudySessionSummary` guarantees controlled version compatibility across deployments.

---

### Q8: What does the `transient` keyword do? Give an example from your project.
**Answer:**  
The `transient` keyword prevents specific instance variables from being serialized into the byte stream by Java's serialization mechanism.  
In `StudySessionSummary`, fields such as `cachedFormattedDisplay` are marked `transient` because they represent derivative presentation state that can easily be recomputed in memory, avoiding redundant storage. Similarly, in `User`, `passwordHash` is marked `transient` so that if a `User` object is ever placed in an HTTP session that gets persisted to disk by Tomcat across server restarts, the sensitive password hash is never serialized to storage.

---

### Q9: How is session fixation prevented in StudySync?
**Answer:**  
Session fixation occurs when an attacker tricks a victim into authenticating using a known, pre-existing session ID, allowing the attacker to hijack the authenticated session.  
In `LoginServlet`, upon successful password verification, we immediately invoke:
```java
req.changeSessionId();
```
This Jakarta Servlet API method invalidates the previous anonymous session ID and assigns a brand-new cryptographically random session ID while seamlessly preserving session attributes, neutralizing session fixation.

---

### Q10: What is Cross-Site Request Forgery (CSRF) and how is it mitigated in your application?
**Answer:**  
CSRF is an attack where an unauthorized site tricks an authenticated user's browser into executing unwanted actions on a trusted site where the user is currently authenticated (via automatic browser cookie transmission).  
StudySync implements the **Synchronizer Token Pattern**:
1. When a session is initiated, `CsrfUtil` generates a cryptographically secure random token (`SecureRandom` + Base64URL) and stores it in the user's session.
2. Every HTML form (`<form method="post">`) includes a hidden input `<input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />`.
3. `CsrfFilter` intercepts every incoming `POST`, `PUT`, and `DELETE` request and performs a constant-time comparison (`MessageDigest.isEqual`) between the request parameter and the session token. Requests missing or with mismatched tokens are rejected with HTTP `403 Forbidden`.

---

### Q11: Explain how database transactions are managed in StudySync.
**Answer:**  
In JDBC, auto-commit is enabled by default. To execute multi-step operations atomically, we disable auto-commit:
```java
conn.setAutoCommit(false);
```
In `StudySessionDAO.createSessionWithParticipants()`, three interdependent SQL statements must succeed together:
1. Creating the parent `study_sessions` row.
2. Inserting participant records into `session_participants`.
3. Updating the originating `study_requests` status to `ACCEPTED`.
If all operations succeed, `conn.commit()` is called. If any `SQLException` occurs, `conn.rollback()` is executed inside the `catch` block to revert the database to its pristine state, satisfying the Atomicity requirement of ACID. Finally, auto-commit is reset to true in the `finally` block before returning the connection to the pool.

---

### Q12: Why are JSPs located under the `/WEB-INF/` directory?
**Answer:**  
The Java Servlet Specification mandates that the `/WEB-INF/` directory is completely protected from direct HTTP client requests. If a user tries to access `http://localhost:8080/StudySync/WEB-INF/views/dashboard.jsp` directly, Tomcat returns an HTTP `404 Not Found`.  
This enforces the **MVC Architecture**: clients can only interact with servlets (`/dashboard`), which perform authentication checks, load models via services, bind attributes to the request, and internally forward (`req.getRequestDispatcher().forward()`) to the JSP for view rendering.

---

### Q13: Why are scriptlets (`<% ... %>`) considered bad practice in JSP?
**Answer:**  
Scriptlets mix raw Java business logic with HTML presentation markup, leading to unmaintainable, error-prone spaghetti code that violates the Single Responsibility Principle. Scriptlets also make automated code auditing and XSS prevention difficult.  
Modern JSP standards enforce the use of **JSTL (Jakarta Standard Tag Library)** and **EL (Expression Language)**:
- `<c:out value="${student.name}" />` automatically applies HTML-entity escaping, preventing Cross-Site Scripting (XSS).
- `<c:forEach>` and `<c:if>` provide declarative control flow.

---

### Q14: How does connection pooling improve application performance?
**Answer:**  
Establishing a physical database connection involves costly socket creation, TLS handshakes, and authentication negotiation, taking tens or hundreds of milliseconds per request.  
A connection pool (like Apache Commons DBCP2 in `DBUtil`) pre-allocates a pool of active database connections upon application startup. When a servlet requests a connection (`DBUtil.getConnection()`), an existing idle connection is checked out in microseconds. When the `try-with-resources` block closes the connection, the wrapper's `close()` method does not terminate the physical socket; instead, it returns the connection back to the pool for reuse.

---

### Q15: How does BCrypt differ from standard SHA-256 for password hashing?
**Answer:**  
SHA-256 is a general-purpose cryptographic hash function designed to be fast, making it susceptible to brute-force and GPU-accelerated rainbow table attacks (calculating billions of hashes per second).  
BCrypt is an adaptive key-derivation function based on the Blowfish cipher. It incorporates:
1. **Automatic Salting:** A cryptographically random salt is embedded into the hash output, preventing pre-computed rainbow table attacks.
2. **Work Factor (Cost Parameter):** In StudySync, we use cost factor 10 ($2^{10} = 1024$ rounds of key expansion). This intentionally slows down hash computation (~100 ms per check), rendering offline brute-force attacks computationally infeasible while remaining fast enough for legitimate logins.

---

## 3. Design Trade-Offs & Architecture Rationales

| Decision | Chosen Approach | Alternative Considered | Rationale |
|---|---|---|---|
| **Persistence** | Raw JDBC with `PreparedStatement` & DBCP2 | Hibernate / Spring Data JPA | Zero hidden magic; student can explain every SQL query, join, and transaction in the viva. |
| **Dependency Injection** | Constructor & Manual Service Composition | Spring Framework / Guice | No external runtime container needed; clear object lifecycle understanding. |
| **View Layer** | JSP 3.1 + JSTL 3.0 (Jakarta EE) | Thymeleaf / React SPA | Native Servlet engine integration without requiring Node.js or JavaScript build tools. |
| **Password Security** | BCrypt via `jBCrypt` | Plain SHA-256 / MD5 | Production-standard adaptive hashing with embedded salt prevents rainbow tables. |
| **File Storage** | Filesystem storage with DB metadata | Storing file BLOBs in MySQL | Prevents database bloat, allows zero-copy streaming, and follows twelve-factor storage principles. |
| **CSRF Defense** | Synchronizer Token Pattern via Filter | Origin / Referer header check | Cryptographically secure token bound to session; immune to header stripping or proxy anomalies. |
