# StudySync JavaFX WebView launcher plan

## Confirmed scope

Build a thin JavaFX desktop launcher for the existing StudySync JSP site. It
loads the locally running Tomcat application in a JavaFX `WebView`.

Default URL:

```text
http://localhost:8080/StudySync/login
```

When Tomcat or StudySync is unavailable, show a clear native error view with a
**Retry** button. The desktop client is run from `desktop-client` using:

```powershell
mvn javafx:run
```

Tomcat and MySQL must be started separately first. The launcher does not deploy,
start, stop, or reconfigure them.

## Scope boundaries

There are no backend changes:

- Do not add JSON APIs, API servlets, or DTOs.
- Do not modify JSPs, filters, services, DAOs, SQL scripts, or existing tests.
- Do not access MySQL/JDBC from JavaFX.
- Do not recreate login, dashboard, profile, buddies, notes, requests, sessions,
  ratings, or admin pages as native JavaFX views.
- No Windows installer is required.

The final architecture is:

```text
JavaFX window -> WebView -> existing JSP/CSS/JavaScript -> Tomcat -> MySQL
```

JavaFX supplies only the native outer window and browser controls. StudySync
remains a server-side-rendered JSP application; its servlet session, CSRF form
tokens, redirects, validation, uploads, downloads, and authorization behave as
they do in a normal browser.

## Benefits and known limitations

This is the correct choice when JavaFX is required but rewriting the web UI is
not. It preserves the tested backend and avoids maintaining two frontends.

Limitations to state in the submission:

- It is a desktop wrapper, not a native JavaFX replacement of the website.
- It requires local Tomcat and MySQL.
- JavaFX WebView uses WebKit rather than Chrome; the existing JSP/CSS/JS should
  be tested in the WebView.
- The session normally lasts for the current launcher run; users may need to log
  in again after closing the desktop program.
- Browser popups, external links, and downloads may need explicit WebView
  handling.

## Repository layout

Keep the existing WAR project untouched. Add an isolated Maven project instead
of immediately converting the repository into a multi-module reactor.

```text
StudySync/
├── pom.xml                         # existing backend WAR build: unchanged
├── src/                             # existing backend: unchanged
├── database/                        # existing database files: unchanged
└── desktop-client/
    ├── pom.xml
    └── src/main/
        ├── java/com/studysync/desktop/
        │   ├── StudySyncLauncher.java
        │   ├── config/AppConfig.java
        │   └── controller/WebViewController.java
        └── resources/com/studysync/desktop/
            ├── launcher.fxml
            ├── error.fxml
            ├── css/launcher.css
            └── application.properties
```

## Desktop design

### Configuration

Put these values in `application.properties`:

```properties
studysync.start-url=http://localhost:8080/StudySync/login
studysync.allowed-origin=http://localhost:8080/StudySync/
```

`AppConfig` loads and validates the URLs. Controllers must not hard-code the
server address. The property lets an evaluator adjust a Tomcat port or context
path without changing Java source.

### Main window and FXML

`launcher.fxml` contains:

- A `WebView` occupying the application content area.
- Back, Forward, Reload, and Home controls.
- A small loading indicator/status text.

Recommended stage settings:

- Title: `StudySync Desktop`
- Initial size: 1200 × 800
- Minimum size: 900 × 600

Back and Forward reflect `WebEngine.getHistory()`. Reload reloads the active
page. Home always loads the configured login URL. Do not add a JavaFX login
page or native navigation sidebar.

### WebView behavior

- Call `webEngine.setJavaScriptEnabled(true)` so existing `app.js` works.
- Let the WebView own its HTTP session cookies. Do not add custom cookie code.
- Do not use `executeScript` except for a narrowly documented compatibility
  fix; existing page scripts must work naturally.
- Use the `WebEngine` load worker for progress and failure detection.
- Never make blocking network calls on the JavaFX application thread.

### Navigation safety

The WebView should be limited to URLs under `studysync.allowed-origin`:

- Allow normal internal StudySync links, redirects, form submissions, and
  fragments.
- Block URLs outside the configured origin and show a short status message.
- Block `file:`, `javascript:`, and `data:` schemes.
- Handle `window.open()` through `createPopupHandler`: allowed StudySync targets
  may load in the existing WebView; all other popups are blocked.

This prevents the application from becoming an unrestricted embedded browser.

### Failure and Retry screen

If `WebEngine.getLoadWorker()` reaches `FAILED`, show `error.fxml` or a native
overlay containing:

- The configured StudySync URL.
- A plain explanation that Tomcat/StudySync cannot be reached.
- A reminder to start MySQL and Tomcat and deploy the StudySync WAR.
- A **Retry** button that retries the configured start URL.
- Optional technical detail from the load exception, without showing a raw Java
  stack trace in the normal user message.

Retry must work if the user starts Tomcat after the JavaFX application is
already open.

## Phase-wise coding plan for a lower-capability agent

### Rules for every phase

1. Read the listed files before editing.
2. Do not edit anything outside `desktop-client`.
3. Do not replace any JSP page with an FXML page.
4. Keep each commit/change limited to its phase.
5. Run the phase verification before continuing.

### Phase 0 — Verify the existing web application

**Goal:** Ensure there is a working JSP site to wrap.

**Tasks:**

1. From the repository root, run:
   ```powershell
   mvn clean test
   ```
2. Start MySQL and Tomcat using the existing project instructions.
3. Open `http://localhost:8080/StudySync/login` in a normal browser.
4. Verify login, one protected page, and logout.
5. If the port/context differs, use that actual URL in the launcher properties.

**Do not edit:** backend Java, JSPs, filters, database, root `pom.xml`.

**Done when:** the website works in a normal browser at the configured URL.

### Phase 1 — Scaffold the JavaFX Maven project

**Goal:** A blank JavaFX window opens with Maven.

**Create:**

```text
desktop-client/pom.xml
desktop-client/src/main/java/com/studysync/desktop/StudySyncLauncher.java
desktop-client/src/main/resources/com/studysync/desktop/application.properties
```

**Tasks:**

1. Match the backend's Java 17 release.
2. Add JavaFX Controls, FXML, and Web dependencies, plus the JavaFX Maven
   plugin. Use a JavaFX version compatible with Java 17.
3. Make `StudySyncLauncher` extend `Application`.
4. Set title, 1200 × 800 initial dimensions, and 900 × 600 minimum dimensions.
5. Configure the module so this works:
   ```powershell
   Set-Location desktop-client
   mvn javafx:run
   ```

**Done when:** an empty JavaFX window launches and closes cleanly.

### Phase 2 — Add the configured WebView shell

**Goal:** Render the real JSP login page in the desktop application.

**Read only:**

```text
src/main/webapp/index.jsp
src/main/webapp/WEB-INF/views/login.jsp
src/main/webapp/js/app.js
```

**Create:**

```text
config/AppConfig.java
controller/WebViewController.java
resources/com/studysync/desktop/launcher.fxml
resources/com/studysync/desktop/css/launcher.css
```

**Tasks:**

1. Load and validate the start URL and allowed origin from properties.
2. Create FXML with a toolbar, status/progress region, and `WebView`.
3. Load the configured start URL after the stage is shown.
4. Enable JavaScript on the WebEngine.
5. Apply CSS only to the JavaFX outer shell; reuse the website's own CSS inside
   the WebView naturally.

**Verify:** with Tomcat running, use the WebView to submit the existing JSP
login form.

**Done when:** the actual JSP login page appears and successfully authenticates;
no JavaFX login form exists.

### Phase 3 — Browser controls and session-flow check

**Goal:** Add predictable user controls without replacing browser behavior.

**Tasks:**

1. Add Back/Forward with `WebEngine.getHistory()`.
2. Disable each control when its corresponding history action is unavailable.
3. Reload the current page; Home reloads the configured start URL.
4. Show a loading indicator only while the WebEngine worker is scheduled or
   running.
5. Test login, dashboard navigation, profile update, logout, and Home.

**Do not:** implement login calls, HTTP clients, CSRF tokens, or cookie storage
in Java. The existing JSP app handles those responsibilities.

**Done when:** the existing user session remains usable through normal WebView
navigation and the shell controls work.

### Phase 4 — Failure detection and Retry

**Goal:** Make a stopped Tomcat server understandable and recoverable.

**Create:**

```text
resources/com/studysync/desktop/error.fxml
controller/ErrorController.java   # only if separate from WebViewController
```

**Tasks:**

1. Observe `WebEngine.getLoadWorker().stateProperty()` and its exception.
2. When the worker becomes `FAILED`, show a native error screen/overlay, not a
   blank WebView.
3. Display the configured URL and the startup instruction.
4. Retry must attempt the configured start URL again.
5. Clear/stop the progress indicator after success or failure.

**Verification:**

1. Stop Tomcat.
2. Start `mvn javafx:run`.
3. Confirm the error screen and Retry button appear.
4. Start Tomcat without closing JavaFX.
5. Press Retry and confirm the JSP login page appears.

**Done when:** recovery succeeds without relaunching the JavaFX program.

### Phase 5 — Navigation/popup restriction

**Goal:** Keep the embedded browser restricted to StudySync.

**Tasks:**

1. Parse each requested URL before navigation.
2. Allow only HTTP/HTTPS URLs that are under the configured origin.
3. Block external URLs and unsafe schemes with a short status message.
4. Configure `createPopupHandler` so it does not create uncontrolled windows.
5. Allow an internal popup target only by loading it in the existing WebView.

**Done when:** all normal StudySync navigation works, while external and unsafe
locations are rejected.

### Phase 6 — Regression demonstration

**Goal:** Confirm the wrapper preserves required JSP features.

**Test within the WebView:**

1. Login and logout.
2. Registration and a validation failure.
3. Dashboard and profile update.
4. Buddy search and a study-request action.
5. Session list/detail action.
6. Notes list/search; include upload/download only if required for submission.
7. Admin login and one protected admin page, if admin is in scope.
8. Refresh and Back/Forward after a form submission.

Rerun the backend regression suite from the root:

```powershell
mvn clean test
```

**Done when:** required workflows work in WebView and the backend suite passes
without backend edits.

## Prompt for the coding agent

> Create a thin JavaFX WebView launcher only in `desktop-client`. Do not modify
> existing backend files, the root Maven build, JSPs, servlets, filters,
> services, DAOs, SQL, or tests. Use Java 17 and Maven. Load
> `http://localhost:8080/StudySync/login` from `application.properties`; do not
> recreate any StudySync web page in JavaFX. Add Back, Forward, Reload, Home,
> progress status, JavaScript support, and a native Retry error screen for an
> unavailable Tomcat server. Keep all loaded URLs within the configured
> `http://localhost:8080/StudySync/` origin and block unsafe/external schemes.
> The project must run from `desktop-client` using `mvn javafx:run`.

## Submission runbook

1. Start MySQL.
2. Build/deploy the existing StudySync WAR to local Tomcat.
3. Confirm `http://localhost:8080/StudySync/login` works in a browser.
4. Open PowerShell in `desktop-client`.
5. Run:
   ```powershell
   mvn javafx:run
   ```
6. Demonstrate login and a protected JSP workflow inside the JavaFX WebView.
