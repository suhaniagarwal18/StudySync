package com.studysync.model;

import com.studysync.exception.ResourceNotFoundException;
import com.studysync.service.SessionSummaryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests verifying Java Object Serialization, deserialization, and transient keyword mechanics.
 * 
 * CONCEPT: Java Serialization (ObjectOutputStream / ObjectInputStream / transient)
 */
class SerializationTest {

    @Test
    @DisplayName("StudySessionSummary binary serialization round-trip must preserve fields and honor transient modifier")
    void testSerializationRoundTrip() throws Exception {
        StudySession session = new StudySession();
        session.setSessionId(42);
        session.setSubject("Database Systems");
        session.setSessionDate(LocalDate.of(2026, 11, 15));
        session.setSessionTime(LocalTime.of(14, 0));
        session.setStatus(SessionStatus.COMPLETED);

        Student s1 = new Student(1, "Aarav Sharma", "aarav@edu", "hash", "CS");
        Student s2 = new Student(2, "Diya Patel", "diya@edu", "hash", "IT");
        List<Student> parts = new ArrayList<>();
        parts.add(s1);
        parts.add(s2);
        session.setParticipants(parts);

        StudySessionSummary original = new StudySessionSummary(session, "Aarav");
        // Compute transient display on original object
        String originalSummaryDisplay = original.getSummaryDisplay();
        assertNotNull(originalSummaryDisplay);

        // 1. Serialize object into memory byte array using ObjectOutputStream
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (ObjectOutputStream objOut = new ObjectOutputStream(byteOut)) {
            objOut.writeObject(original);
            objOut.flush();
        }

        byte[] serializedData = byteOut.toByteArray();
        assertTrue(serializedData.length > 0, "Serialized byte stream should not be empty");

        // 2. Deserialize back using ObjectInputStream
        StudySessionSummary deserialized;
        try (ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(serializedData))) {
            Object obj = objIn.readObject();
            assertTrue(obj instanceof StudySessionSummary);
            deserialized = (StudySessionSummary) obj;
        }

        // Verify restored non-sensitive state
        assertEquals(42, deserialized.getSessionId());
        assertEquals("Database Systems", deserialized.getSubject());
        assertEquals(LocalDate.of(2026, 11, 15), deserialized.getSessionDate());
        assertEquals(LocalTime.of(14, 0), deserialized.getSessionTime());
        assertEquals("COMPLETED", deserialized.getStatus());
        assertEquals(2, deserialized.getParticipantNames().size());

        // Verify transient field was NOT serialized (reflection confirms raw field is null after deserialization)
        Field transientField = StudySessionSummary.class.getDeclaredField("cachedSummaryDisplay");
        transientField.setAccessible(true);
        Object transientValue = transientField.get(deserialized);
        assertNull(transientValue, "transient field 'cachedSummaryDisplay' must be null upon deserialization");

        // Calling getter should lazily reconstruct the display without crashing
        assertEquals(originalSummaryDisplay, deserialized.getSummaryDisplay());
    }

    @Test
    @DisplayName("Loading non-existent summary file should throw ResourceNotFoundException")
    void testMissingSummaryFile() {
        SessionSummaryService service = new SessionSummaryService();
        Student fakeUser = new Student(999, "Fake", "fake@edu", "hash", "CS");

        assertThrows(ResourceNotFoundException.class, () -> service.loadSummary(999999, fakeUser));
    }
}
