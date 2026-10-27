# StudySync JavaFX migration: implementation and coding plan

## 1. Goal and non-negotiable boundaries

Build a JavaFX desktop application that consumes a JSON API exposed by the
existing StudySync Tomcat application. Keep the JSP application available
throughout development. The desktop client must never use JDBC, database
credentials, DAO classes, or backend service classes directly.

The system after this work is:

```text
JavaFX desktop client -- HTTPS/HTTP + JSON --> StudySync WAR on Tomcat --> services --> DAOs --> MySQL
JSP browser client   -- existing servlet flow  --> StudySync WAR on Tomcat --> services --> DAOs --> MySQL
```

Existing project facts:

- The backend is a Java 17 Maven WAR (`studysync`) using Jakarta Servlet 6.
- Existing browser controllers are in `src/main/java/com/studysync/servlet`.
- It already has `AuthService`, `ProfileService`, `MatchingService`,
  `StudyRequestService`, `StudySessionService`, `NoteService`, `RatingService`,
  and `AdminService`.
- `AuthFilter` currently redirects unauthenticated users to `/login`.
- `CsrfFilter` currently validates normal POST form parameters globally.

Those filters must provide API-specific JSON behavior before JavaFX coding
begins. Do not modify existing JSP servlet behavior as part of the API work.

## 2. Target repository layout

Convert the root project into a Maven reactor while preserving the existing
backend module's source layout. The least disruptive layout is:

```text
StudySync/
├── pom.xml                         # parent/aggregator, packaging pom
├── backend/
│   ├── pom.xml                     # existing WAR dependencies/build
│   └── src/                        # move existing src here only in this phase
├── desktop-client/
│   ├── pom.xml                     # JavaFX application JAR
│   └── src/main/
│       ├── java/com/studysync/desktop/
│       └── resources/com/studysync/desktop/
│           ├── fxml/
│           ├── css/
│           └── application.properties
├── shared-contracts/                # optional, pure API DTOs only
│   ├── pom.xml
│   └── src/main/java/com/studysync/api/dto/
└── database/
```

If moving the existing source tree is too disruptive for the current milestone,
keep the root project as the backend WAR and add `desktop-client` as a separate
Maven project first. Do **not** make `desktop-client` depend on the root WAR.
Only introduce `shared-contracts` after the API response shapes are stable; it
may contain records/enums but never services, DAOs, servlet classes, or database
configuration.

## 3. API conventions

Base path: `/api/v1` within the existing WAR context path. A local development
URL is therefore normally `http://localhost:8080/StudySync/api/v1`.

### 3.1 HTTP and JSON rules

- Request content type: `application/json; charset=UTF-8`, except uploads.
- Response content type: `application/json; charset=UTF-8`.
- `GET` never mutates state. Use `POST`, `PUT`, or `DELETE` for mutations.
- Use ISO-8601 strings: `yyyy-MM-dd` for dates and `HH:mm:ss` for times.
- Return only DTOs. Never serialize `User`, `Student`, `Note`, or any database
  model directly.
- Never return password hashes, internal storage paths, SQL errors, stack traces,
  CSRF secrets, or another user's private information.
- An API servlet must never forward to a JSP or send a redirect.

### 3.2 Uniform envelopes

Success may return the resource directly. Error responses always use:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Email is already registered",
  "fieldErrors": { "email": "Already in use" }
}
```

Use these status codes consistently:

| Situation | HTTP status |
| --- | --- |
| Successful read/update | 200 |
| Successful creation | 201 |
| Successful delete without body | 204 |
| Invalid JSON or validation failure | 400 |
| No active session | 401 |
| Authenticated but not permitted | 403 |
| Missing resource | 404 |
| Duplicate/conflicting state | 409 |
| Unexpected failure | 500 with safe generic message |

### 3.3 Initial endpoint contract

Implement only the endpoints that map to existing functionality. Exact field
names should be documented in tests and remain backward-compatible once used by
the desktop client.

| Endpoint | Method | Request / response purpose | Existing service |
| --- | --- | --- | --- |
| `/auth/csrf` | GET | Create/read a session and return a CSRF token | `CsrfUtil` |
| `/auth/login` | POST | `{email,password}` -> safe current-user DTO | `AuthService` |
| `/auth/register` | POST | registration fields -> created safe user DTO | `AuthService` |
| `/auth/logout` | POST | Invalidate current session | servlet session |
| `/me` | GET | Current safe profile/role | `ProfileService` |
| `/me` | PUT | Update current user's editable profile fields | `ProfileService` |
| `/dashboard` | GET | Existing dashboard counts and upcoming sessions | existing services |
| `/skills` | GET | Available skill values | `ProfileService` |
| `/buddies` | GET | Matches; optional `subject` query | `MatchingService` |
| `/study-requests` | GET | Received and sent request lists | `StudyRequestService` |
| `/study-requests` | POST | Send request: receiver id and subject | `StudyRequestService` |
| `/study-requests/{id}/accept` | POST | Accept one request | `StudyRequestService` |
| `/study-requests/{id}/reject` | POST | Reject one request | `StudyRequestService` |
| `/sessions` | GET | Current user's sessions | `StudySessionService` |
| `/sessions` | POST | Schedule from an accepted request | `StudySessionService` |
| `/sessions/{id}` | GET | One authorized session | `StudySessionService` |
| `/sessions/{id}/cancel` | POST | Cancel an authorized session | `StudySessionService` |
| `/sessions/{id}/complete` | POST | Mark an authorized session complete | `StudySessionService` |
| `/sessions/{id}/ratings` | POST | Submit rating for completed session | `RatingService` |
| `/notes` | GET | Search notes; `keyword`/`subject` optional | `NoteService` |
| `/notes` | POST multipart | Upload note + metadata | existing upload/service logic |
| `/notes/{id}` | DELETE | Delete only if owner/authorized | `NoteService` |
| `/notes/{id}/download` | GET | Stream authorized download | note/download logic |

Admin routes are deliberately deferred. Later, place them below
`/api/v1/admin/*` and apply both session authentication and admin role checks.

## 4. Backend implementation design

### 4.1 New packages

Create these backend packages:

```text
com.studysync.api.servlet     API servlets only
com.studysync.api.dto         request/response DTO records
com.studysync.api.util        JSON input/output and API error helpers
com.studysync.api.filter      optional API-specific filters
```

Suggested reusable helpers:

- `JsonUtil`: creates/configures one JSON mapper and reads/writes JSON.
- `ApiResponseUtil`: writes a JSON response/error with status and UTF-8 headers.
- `ApiExceptionMapper`: maps existing `StudySyncException` subclasses to status
  codes and safe error bodies.
- `ApiRequestUtil`: reads a JSON DTO, rejects malformed bodies, extracts integer
  path components safely.
- `CurrentUserUtil`: obtains the authenticated `User` from the session, without
  trusting a user ID supplied by the client.

Add one JSON implementation dependency, preferably Jackson Databind, to the
backend Maven dependencies. Configure the date/time module explicitly; do not
depend on reflection defaults. Keep the dependency in the WAR runtime classpath.

### 4.2 Authentication and CSRF adaptation

This is mandatory because the global filters currently assume browser/JSP flows.

1. In `AuthFilter`, detect `path.startsWith("/api/")`.
2. Public API paths are only `/api/v1/auth/csrf`, `/api/v1/auth/login`, and
   `/api/v1/auth/register`.
3. For a protected API path with no active user, return `401` JSON; never
   redirect to `/login`.
4. For a browser route, keep the current redirect behavior unchanged.
5. Extend `CsrfUtil`/`CsrfFilter` to accept a header such as `X-CSRF-Token` in
   addition to its existing form parameter. Require it for API mutations.
6. `GET /api/v1/auth/csrf` creates a session if necessary and returns
   `{ "csrfToken": "..." }`. The JavaFX client calls it before register/login
   and refreshes it after login because session rotation can invalidate state.
7. The JavaFX client must retain Tomcat's `JSESSIONID` cookie using a
   `CookieManager` with `CookiePolicy.ACCEPT_ALL`.
8. On login, call `request.changeSessionId()` before storing the user exactly as
   the JSP login servlet does. On logout, invalidate the session.
9. Do not add broad CORS headers. A desktop Java `HttpClient` is not restricted
   by browser CORS, and permissive CORS would unnecessarily enlarge exposure for
   the JSP session cookie.

### 4.3 Authorization rules

- The active session determines the caller. Never accept `currentUserId` in a
  request body.
- For request acceptance/rejection, sessions, ratings, notes, and downloads,
  call the existing service methods that enforce ownership/participation.
- Role checks must be performed in endpoint/filter logic even if the JavaFX UI
  hides admin navigation.
- Do not expose an endpoint for serialized session-summary files unless there
  is a product requirement; this is a server-side teaching feature, not a
  desktop workflow requirement.

### 4.4 Endpoint sequencing

Create individual servlets rather than one large action-switch servlet. Examples:

```text
ApiAuthServlet            /api/v1/auth/*
ApiCurrentUserServlet     /api/v1/me
ApiDashboardServlet       /api/v1/dashboard
ApiBuddyServlet           /api/v1/buddies
ApiStudyRequestServlet    /api/v1/study-requests/*
ApiStudySessionServlet    /api/v1/sessions/*
ApiNoteServlet            /api/v1/notes/*
```

Use `@WebServlet` mappings. Each servlet may route a small number of closely
related subpaths, but a servlet should not become a replacement for the entire
application router.

## 5. JavaFX desktop implementation design

### 5.1 Dependencies and launch

Use Java 17 and a JavaFX version compatible with it. The desktop Maven module
needs JavaFX Controls, FXML, a JSON library matching the backend contract, and
the JavaFX Maven plugin for `javafx:run`. Configure platform-specific JavaFX
artifacts through a Maven profile or the current development OS classifier.

Package only after functional testing, using `jlink`/`jpackage` to produce a
Windows installer. The client should read its base API URL from:

1. a command-line option, then
2. `application.properties`, then
3. a safe localhost default for development.

### 5.2 Desktop package layout

```text
com.studysync.desktop
├── StudySyncApplication.java
├── config/AppConfig.java
├── api/ApiClient.java
├── api/ApiException.java
├── api/dto/                 # desktop-side request/response contract DTOs
├── auth/SessionManager.java
├── navigation/SceneManager.java
├── controller/
├── validation/ClientValidation.java
└── util/FxThread.java
```

`ApiClient` owns one `HttpClient`, `CookieManager`, base URI, JSON mapper, and
CSRF token. Controllers must not construct `HttpClient` objects or manually
parse JSON. `SessionManager` owns the current safe-user DTO and clears it on
logout or `401` responses. `SceneManager` loads FXML and swaps screens.

All network calls run in `CompletableFuture` or JavaFX `Task`; all JavaFX view
updates use `Platform.runLater`/task success handlers. Never block the JavaFX
application thread with `HttpClient.send`.

### 5.3 Screens

Build these FXML files and controllers in order:

| FXML | Controller | Key behavior |
| --- | --- | --- |
| `login.fxml` | `LoginController` | CSRF handshake, login, field/server errors |
| `register.fxml` | `RegisterController` | client validation, register, route to login |
| `shell.fxml` | `ShellController` | navigation, current user, logout |
| `dashboard.fxml` | `DashboardController` | counts and upcoming sessions |
| `profile.fxml` | `ProfileController` | profile view/edit and skills |
| `buddies.fxml` | `BuddyController` | list/filter matches and initiate request |
| `requests.fxml` | `StudyRequestController` | sent/received lists, accept/reject/send |
| `sessions.fxml` | `SessionController` | list/create/cancel/complete |
| `session-details.fxml` | `SessionDetailsController` | detail and rating submission |
| `notes.fxml` | `NoteController` | filter/list/delete/download/upload |

Use JavaFX dialogs for confirmation and error feedback. Reuse shared CSS classes
instead of constructing all styles in controller Java code.

## 6. Definition of done

The migration MVP is done only when all conditions below hold:

- `mvn clean test` for the backend passes.
- Existing JSP login, registration, dashboard, profile, buddy, request, session,
  note, and admin flows remain functional.
- API errors are JSON and never HTML redirects/JSP pages.
- A JavaFX user can register, log in, close/reopen views within one run, log out,
  and complete the student workflows without direct database access.
- JavaFX survives malformed input, server validation errors, timeouts, and a
  `401` response with readable user-facing feedback.
- File upload/download checks type, size, ownership, and failure handling.
- Authorization is demonstrated by tests for another user's request/session/note.
- The desktop client can be started with a documented base API URL and is
  packageable for Windows.

## 7. Phase-wise coding plan for a lower-capability coding agent

### General instructions for every phase

1. Read the listed existing files before editing.
2. Do not refactor unrelated JSP code.
3. Make the smallest focused change and add tests before declaring the phase done.
4. Run the listed verification commands. Stop and report the exact failure if a
   test fails; do not hide it by weakening tests.
5. Never store plaintext passwords, database credentials, or session IDs in
   source control.

### Phase 0 — Baseline and contract decision

**Goal:** Create a reproducible starting point and lock the first API shapes.

**Read:** `pom.xml`, `web.xml`, `AuthFilter.java`, `CsrfFilter.java`,
`LoginServlet.java`, `RegisterServlet.java`, service classes.

**Tasks:**

1. Run `mvn clean test` and record the result in the task output.
2. Add `docs/api-v1-contract.md` containing the auth, `/me`, and dashboard
   request/response examples and the standard error envelope.
3. Choose Jackson and add its exact Maven dependency to the backend build.
4. Do not create JavaFX screens yet.

**Acceptance:** build tests pass and the contract describes only endpoints that
will be implemented in Phase 1–3.

### Phase 1 — JSON infrastructure and API-aware filters

**Goal:** Make `/api/v1` safe and predictable without changing browser routes.

**Read:** Phase 0 files plus `CsrfUtil.java`, all exception classes.

**Create:** `api/util/JsonUtil.java`, `ApiResponseUtil.java`,
`ApiExceptionMapper.java`, and API filter tests where feasible.

**Tasks:**

1. Add JSON serialization/deserialization helpers with UTF-8 content type.
2. Add a DTO for the error envelope.
3. Change `AuthFilter`: protected API calls without a user receive JSON `401`;
   existing non-API calls retain redirects.
4. Change CSRF validation to accept `X-CSRF-Token` for API mutations while
   keeping the existing form token behavior.
5. Add `GET /api/v1/auth/csrf`, public and JSON-only.
6. Write tests/manual HTTP checks for: API unauthenticated `401` JSON; browser
   protected route still redirects; valid/invalid API CSRF behavior.

**Acceptance:** no API request receives a JSP/redirect; JSP login still works.

### Phase 2 — Authentication API

**Goal:** Allow a client to establish and destroy a secure servlet session.

**Read:** `LoginServlet.java`, `LogoutServlet.java`, `RegisterServlet.java`,
`AuthService.java`, `User.java`, validation utilities.

**Create:** auth request DTOs, safe-user response DTO, `ApiAuthServlet`.

**Tasks:**

1. Implement register using the same `AuthService` validation/business rules.
2. Implement login using `AuthService.authenticate`; rotate session ID; store
   `AuthFilter.SESSION_USER`; return only id/name/email/role.
3. Implement logout by invalidating the session and returning `204`.
4. Map invalid credentials to a safe `401` message; do not indicate whether an
   email address exists.
5. Add tests for success, invalid password, validation error, duplicate email,
   CSRF required, and logout.

**Acceptance:** a cookie-preserving HTTP client can CSRF-handshake, login, call
a protected endpoint, log out, then receive `401`.

### Phase 3 — Profile and dashboard API

**Goal:** Prove the API pattern on simple authenticated reads/updates.

**Read:** `ProfileServlet.java`, `DashboardServlet.java`, `ProfileService.java`,
`NoteService.java`, `RatingService.java`, model classes used by those services.

**Create:** profile/dashboard DTOs, `ApiCurrentUserServlet`,
`ApiDashboardServlet`, `ApiSkillServlet`.

**Tasks:**

1. Implement `GET /me`, `PUT /me`, `GET /skills`, `GET /dashboard`.
2. Copy dashboard aggregation logic from `DashboardServlet` only conceptually;
   ideally extract it to a reusable service method instead of duplicating it.
3. Return ISO values and numeric counts, not display-formatted strings.
4. Test that profile updates apply only to the session user.

**Acceptance:** endpoints return valid JSON DTOs and no client-controlled user
ID can update another account.

### Phase 4 — Bootstrap the JavaFX client

**Goal:** A runnable desktop app logs in and reaches a protected dashboard.

**Read:** the Phase 1–3 API contract. Do not read or depend on DAO classes.

**Create:** `desktop-client` Maven project; `StudySyncApplication`, `AppConfig`,
`ApiClient`, `SessionManager`, `SceneManager`, login/dashboard FXML and CSS.

**Tasks:**

1. Configure JavaFX Maven dependencies and `javafx:run`.
2. Implement asynchronous `ApiClient` methods for CSRF, login, logout, and
   dashboard. Install a `CookieManager` in its `HttpClient`.
3. Implement `LoginController`: validate blank fields, disable submit while the
   request runs, show server messages, switch on success.
4. Implement a small dashboard proving session cookies are sent correctly.
5. On `401`, clear session state and return to login.

**Acceptance:** `mvn -pl desktop-client javafx:run` opens a login window, logs
in against Tomcat, shows real dashboard data, and logs out.

### Phase 5 — Registration, profile, and shared navigation

**Goal:** Complete basic account workflow and a reusable authenticated shell.

**Read:** Phase 4 classes and profile API contract.

**Create:** register/profile/shell FXML and controllers; validation tests.

**Tasks:**

1. Add navigation between login and register.
2. Implement local validation only for immediate feedback; always display server
   validation as the source of truth.
3. Add shell navigation and a visible logout action.
4. Implement profile load/save and refresh current-user display after save.

**Acceptance:** a new account can register, login, edit its own profile, logout,
and relogin without restarting the backend.

### Phase 6 — Buddy matching and study requests

**Goal:** Implement the core peer-matching workflow end to end.

**Read:** `BuddyServlet.java`, `RequestServlet.java`, `MatchingService.java`,
`StudyRequestService.java`, `MatchResult.java`, `StudyRequest.java`.

**Backend tasks:** add `/buddies` and `/study-requests` endpoints/DTOs, using
existing services and session user id. Test send/accept/reject and unauthorized
actions.

**Desktop tasks:** add `BuddyController` and `StudyRequestController`; render
`TableView` lists; add subject filtering, request creation, accept/reject
confirmation dialogs, refresh after mutation.

**Acceptance:** two test users can send, accept/reject, and view requests;
neither can act on a request belonging to unrelated users.

### Phase 7 — Study sessions and ratings

**Goal:** Schedule, manage, and rate authorized sessions.

**Read:** `SessionServlet.java`, `RatingServlet.java`,
`StudySessionService.java`, `RatingService.java`.

**Backend tasks:** add session list/detail/create/cancel/complete and rating
routes. Parse dates/times strictly. Use the existing services for state rules.

**Desktop tasks:** add session list/detail views, a date/time scheduling dialog,
confirmation before cancel/complete, and rating form after completion.

**Acceptance:** invalid state transitions return safe errors; only participants
can view or mutate a session; a user cannot submit a duplicate rating.

### Phase 8 — Notes and binary transfer

**Goal:** Add the highest-risk workflow after all JSON flows are stable.

**Read:** `NoteServlet.java`, `NoteUploadServlet.java`,
`NoteDownloadServlet.java`, `NoteService.java`, `FileStorageUtil.java`,
existing upload tests/configuration.

**Backend tasks:** add search/list/delete/download and multipart upload routes.
Reuse file type, size, storage, and ownership validation. Return JSON metadata,
but stream download bytes with correct `Content-Type` and safe
`Content-Disposition`. Ensure CSRF header handling works for multipart uploads.

**Desktop tasks:** add note table/search, file chooser, upload progress/error,
download destination chooser, and delete confirmation. Never load a large file
entirely into the UI thread.

**Acceptance:** valid upload/download works; invalid type/oversize/unauthorized
delete fails safely; files are not accessible merely by guessing their ID.

### Phase 9 — Admin, hardening, and packaging

**Goal:** Ship a tested application without expanding privilege boundaries.

**Read:** `AdminFilter.java`, all admin servlets/services, all desktop API code.

**Tasks:**

1. Add `/api/v1/admin/*` with JSON `403` and server-side role checks.
2. Build admin UI only for features with stable existing services.
3. Add API integration tests for authorization, malformed JSON, errors, and
   session expiry.
4. Add desktop unit tests for JSON parsing, API error mapping, and validation.
5. Document environment configuration and run instructions.
6. Configure and test `jpackage` on Windows.

**Acceptance:** full regression suite passes; a packaged desktop application
can use a configured Tomcat URL; all admin APIs reject a student session.

## 8. Commands to use during implementation

Run from the repository root unless the project is still split:

```powershell
mvn clean test
mvn package
mvn -pl desktop-client javafx:run
```

For every API phase, also verify one successful response and one rejected
request using an HTTP client that preserves cookies. Do not mark the work
complete based only on a JavaFX screen appearing; verify server status codes,
JSON response shape, and authorization behavior.
