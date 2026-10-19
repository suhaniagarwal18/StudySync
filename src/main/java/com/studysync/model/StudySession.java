package com.studysync.model;

import com.studysync.exception.InvalidRequestException;
import com.studysync.util.HtmlUtil;
import com.studysync.util.ValidationUtil;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Domain model representing a scheduled study session between two students.
 * 
 * DESIGN DECISION: Composition / Association Over Inheritance
 * WHY: StudySession and StudyRequest are deliberately kept as SEPARATE classes with NO inheritance
 * between them. A StudySession is NOT an enhanced or specialized StudyRequest (there is no true
 * "IS-A" relationship). Instead, they are related via ASSOCIATION: a StudySession references
 * the accepted StudyRequest from which it originated (via requestId). This design models real-world
 * lifecycle separation, preventing fragile base class coupling.
 */
public class StudySession {

    private int sessionId;
    private int requestId;
    private String subject;
    private LocalDate sessionDate;
    private LocalTime sessionTime;
    private SessionStatus status = SessionStatus.SCHEDULED;

    // CONCEPT: Collections (ArrayList for ordered list of participants)
    private List<Student> participants = new ArrayList<>();

    public StudySession() {
    }

    public StudySession(int sessionId, int requestId, String subject, LocalDate sessionDate,
                        LocalTime sessionTime, SessionStatus status) {
        this.sessionId = sessionId;
        this.requestId = requestId;
        this.subject = subject;
        this.sessionDate = sessionDate;
        this.sessionTime = sessionTime;
        this.status = status;
    }

    /**
     * Asserts invariants before scheduling a new study session.
     */
    public void schedule() throws InvalidRequestException {
        if (subject == null || subject.trim().isEmpty()) {
            throw new InvalidRequestException("Subject is required for the study session.");
        }
        if (sessionDate == null) {
            throw new InvalidRequestException("Session date is required.");
        }
        if (sessionDate.isBefore(LocalDate.now())) {
            throw new InvalidRequestException("Session date cannot be scheduled in the past.");
        }
        if (sessionTime == null) {
            throw new InvalidRequestException("Session time is required.");
        }
        if (participants == null || participants.size() != 2) {
            throw new InvalidRequestException("A peer study session requires exactly two distinct student participants.");
        }
        if (participants.get(0).getUserId() == participants.get(1).getUserId()) {
            throw new InvalidRequestException("A session cannot be scheduled between identical participants.");
        }
        this.status = SessionStatus.SCHEDULED;
    }

    /**
     * Cancels an active session. Only valid when status is SCHEDULED.
     */
    public void cancel() throws InvalidRequestException {
        if (this.status != SessionStatus.SCHEDULED) {
            throw new InvalidRequestException("Only SCHEDULED sessions can be cancelled. Current status: " + status);
        }
        this.status = SessionStatus.CANCELLED;
    }

    /**
     * Marks a session as completed so participants can exchange feedback ratings.
     */
    public void complete() throws InvalidRequestException {
        if (this.status != SessionStatus.SCHEDULED) {
            throw new InvalidRequestException("Only SCHEDULED sessions can be marked as completed. Current status: " + status);
        }
        this.status = SessionStatus.COMPLETED;
    }

    /**
     * Checks if a given student ID is one of the session participants.
     */
    public boolean hasParticipant(int userId) {
        for (Student s : participants) {
            if (s.getUserId() == userId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the other participant in this 2-person session.
     */
    public Student getOtherParticipant(int currentUserId) {
        for (Student s : participants) {
            if (s.getUserId() != currentUserId) {
                return s;
            }
        }
        return null;
    }

    // --------------------------------------------------------------------------
    // Formatted View Getters
    // --------------------------------------------------------------------------

    public String getDateDisplay() {
        return HtmlUtil.formatDate(sessionDate);
    }

    public String getTimeDisplay() {
        return HtmlUtil.formatTime(sessionTime);
    }

    // --------------------------------------------------------------------------
    // Getters and Setters
    // --------------------------------------------------------------------------

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public LocalTime getSessionTime() {
        return sessionTime;
    }

    public void setSessionTime(LocalTime sessionTime) {
        this.sessionTime = sessionTime;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public List<Student> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    public void setParticipants(List<Student> participants) {
        this.participants = participants != null ? new ArrayList<>(participants) : new ArrayList<>();
    }

    public void addParticipant(Student student) {
        if (student != null && !this.participants.contains(student)) {
            this.participants.add(student);
        }
    }
}
