package com.studysync.model;

import com.studysync.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests verifying StudySession lifecycle and scheduling constraints.
 */
class StudySessionTest {

    private StudySession session;
    private Student s1;
    private Student s2;

    @BeforeEach
    void setUp() {
        s1 = new Student(1, "Aarav", "aarav@edu", "hash", "CS");
        s2 = new Student(2, "Diya", "diya@edu", "hash", "IT");

        session = new StudySession();
        session.setSessionId(100);
        session.setRequestId(2);
        session.setSubject("Java");
        session.setSessionDate(LocalDate.now().plusDays(2));
        session.setSessionTime(LocalTime.of(15, 30));

        List<Student> parts = new ArrayList<>();
        parts.add(s1);
        parts.add(s2);
        session.setParticipants(parts);
    }

    @Test
    @DisplayName("Valid session scheduling with future date and two distinct participants")
    void testScheduleSuccess() {
        assertDoesNotThrow(() -> session.schedule());
        assertEquals(SessionStatus.SCHEDULED, session.getStatus());
    }

    @Test
    @DisplayName("Scheduling with a past date must throw InvalidRequestException")
    void testPastDateFails() {
        session.setSessionDate(LocalDate.now().minusDays(1));
        assertThrows(InvalidRequestException.class, () -> session.schedule());
    }

    @Test
    @DisplayName("Scheduling without exactly two distinct participants must fail")
    void testParticipantCountValidation() {
        // Only 1 participant
        List<Student> single = new ArrayList<>();
        single.add(s1);
        session.setParticipants(single);
        assertThrows(InvalidRequestException.class, () -> session.schedule());

        // Identical participants
        List<Student> identical = new ArrayList<>();
        identical.add(s1);
        identical.add(s1);
        session.setParticipants(identical);
        assertThrows(InvalidRequestException.class, () -> session.schedule());
    }

    @Test
    @DisplayName("Cancellation is allowed from SCHEDULED status only")
    void testCancellationLifecycle() throws Exception {
        session.schedule();
        assertEquals(SessionStatus.SCHEDULED, session.getStatus());

        // Valid cancel
        assertDoesNotThrow(() -> session.cancel());
        assertEquals(SessionStatus.CANCELLED, session.getStatus());

        // Second cancel must fail
        assertThrows(InvalidRequestException.class, () -> session.cancel());
    }

    @Test
    @DisplayName("Marking session as completed is allowed from SCHEDULED status only")
    void testCompletionLifecycle() throws Exception {
        session.schedule();
        assertDoesNotThrow(() -> session.complete());
        assertEquals(SessionStatus.COMPLETED, session.getStatus());

        // Cannot cancel a completed session
        assertThrows(InvalidRequestException.class, () -> session.cancel());
    }
}
