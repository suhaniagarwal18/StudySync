package com.studysync.dao;

import com.studysync.model.Note;
import com.studysync.model.Rating;
import com.studysync.model.RequestStatus;
import com.studysync.model.SessionStatus;
import com.studysync.model.SkillType;
import com.studysync.model.Student;
import com.studysync.model.StudyRequest;
import com.studysync.model.StudySession;
import com.studysync.model.User;
import com.studysync.util.AppConfig;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test verifying JDBC DAOs against the dedicated test schema 'studysync_test'.
 * 
 * CONCEPT: Database Integration Testing
 * WHY: Tests real PreparedStatement execution, transactions, and foreign keys against an isolated
 * test database without mutating sample or production datasets.
 */
class DatabaseIntegrationTest {

    private static final String TEST_DB_URL = "jdbc:mysql://localhost:3306/studysync_test?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = AppConfig.getDbUser();
    private static final String DB_PASS = AppConfig.getDbPassword();
    private static boolean dbAvailable = false;

    @BeforeAll
    static void checkDatabaseAvailability() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(TEST_DB_URL, DB_USER, DB_PASS)) {
                dbAvailable = (conn != null && !conn.isClosed());
            }
        } catch (Exception e) {
            dbAvailable = false;
        }
    }

    @Test
    @DisplayName("End-to-End DAO Lifecycle in dedicated test schema 'studysync_test'")
    void testFullDaoIntegrationSuite() {
        Assumptions.assumeTrue(dbAvailable, "MySQL is not running or 'studysync_test' schema unavailable. Skipping integration test.");

        UserDAO userDAO = new UserDAO();
        SkillDAO skillDAO = new SkillDAO();
        NoteDAO noteDAO = new NoteDAO();
        StudyRequestDAO requestDAO = new StudyRequestDAO();
        StudySessionDAO sessionDAO = new StudySessionDAO();
        RatingDAO ratingDAO = new RatingDAO();
        StatsDAO statsDAO = new StatsDAO();

        // 1. User creation test
        String uniqueEmail1 = "test.student1." + System.currentTimeMillis() + "@studysync.edu";
        String uniqueEmail2 = "test.student2." + System.currentTimeMillis() + "@studysync.edu";

        Student s1 = new Student();
        s1.setName("Test Student One");
        s1.setEmail(uniqueEmail1);
        s1.setPasswordHash("$2a$10$eSluIiiL/O3Qhts28/MxAOR0HeDFkOxkrDgatrVf6bicqyKm/wHVe");
        s1.setDepartment("Computer Science");
        s1.setActive(true);
        userDAO.create(s1);
        assertTrue(s1.getUserId() > 0);

        Student s2 = new Student();
        s2.setName("Test Student Two");
        s2.setEmail(uniqueEmail2);
        s2.setPasswordHash("$2a$10$eSluIiiL/O3Qhts28/MxAOR0HeDFkOxkrDgatrVf6bicqyKm/wHVe");
        s2.setDepartment("Information Technology");
        s2.setActive(true);
        userDAO.create(s2);
        assertTrue(s2.getUserId() > 0);

        // 2. Skill dictionary and association
        skillDAO.addSkillToUser(s1.getUserId(), "Java", SkillType.KNOWN);
        skillDAO.addSkillToUser(s1.getUserId(), "DBMS", SkillType.NEEDED);
        skillDAO.addSkillToUser(s2.getUserId(), "DBMS", SkillType.KNOWN);
        skillDAO.addSkillToUser(s2.getUserId(), "Java", SkillType.NEEDED);

        assertTrue(skillDAO.getSkills(s1.getUserId(), SkillType.KNOWN).contains("Java"));
        assertTrue(skillDAO.getSkills(s1.getUserId(), SkillType.NEEDED).contains("DBMS"));

        // 3. Study request creation and state transition
        StudyRequest req = new StudyRequest();
        req.setSenderId(s1.getUserId());
        req.setReceiverId(s2.getUserId());
        req.setSubject("DBMS");
        req.setStatus(RequestStatus.PENDING);
        requestDAO.insert(req);
        assertTrue(req.getRequestId() > 0);

        requestDAO.updateStatus(req.getRequestId(), RequestStatus.ACCEPTED);
        StudyRequest updatedReq = requestDAO.findById(req.getRequestId());
        assertNotNull(updatedReq);
        assertEquals(RequestStatus.ACCEPTED, updatedReq.getStatus());

        // 4. Transactional StudySession creation with participants
        StudySession session = new StudySession();
        session.setRequestId(req.getRequestId());
        session.setSubject("DBMS");
        session.setSessionDate(LocalDate.now().plusDays(1));
        session.setSessionTime(LocalTime.of(16, 0));
        session.setStatus(SessionStatus.SCHEDULED);

        sessionDAO.createSession(session, Arrays.asList(s1.getUserId(), s2.getUserId()));
        assertTrue(session.getSessionId() > 0);

        StudySession fetchedSession = sessionDAO.findById(session.getSessionId());
        assertNotNull(fetchedSession);
        assertEquals(2, fetchedSession.getParticipants().size());

        // Complete session
        sessionDAO.updateStatus(session.getSessionId(), SessionStatus.COMPLETED);

        // 5. Rating submission and aggregation
        Rating rating = new Rating();
        rating.setSessionId(session.getSessionId());
        rating.setReviewerId(s1.getUserId());
        rating.setRevieweeId(s2.getUserId());
        try {
            rating.setRatingValue(5);
            rating.setFeedback("Awesome peer mentoring!");
        } catch (Exception ignored) {}
        ratingDAO.insert(rating);
        assertTrue(rating.getRatingId() > 0);

        double avg = ratingDAO.getAverageRatingForUser(s2.getUserId());
        assertEquals(5.0, avg, 0.01);

        // 6. Note insertion, search, and deletion
        Note note = new Note();
        note.setUserId(s1.getUserId());
        note.setTitle("Integration Test Notes on SQL");
        note.setSubject("DBMS");
        note.setModule("Unit 1");
        note.setOriginalFilename("test.pdf");
        note.setStoredFilename("stored-test.pdf");
        note.setFilePath("stored-test.pdf");
        note.setFileSize(1024);
        noteDAO.insert(note);
        assertTrue(note.getNoteId() > 0);

        List<Note> searchResults = noteDAO.search("SQL", "DBMS");
        assertFalse(searchResults.isEmpty());

        noteDAO.delete(note.getNoteId());
        Note deletedNote = noteDAO.findById(note.getNoteId());
        assertEquals(null, deletedNote);
    }
}
