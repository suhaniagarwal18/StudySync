package com.studysync.model;

import com.studysync.util.HtmlUtil;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Portable data transfer object representing a serialized snapshot summary of a completed study session.
 * 
 * CONCEPT: Object Serialization (java.io.Serializable)
 * WHY: Enables saving and restoring session reports via Java's native ObjectOutputStream
 * and ObjectInputStream. Uses an explicit serialVersionUID to guarantee class compatibility
 * during version deserialization.
 * 
 * SECURITY NOTICE: Contains ONLY non-sensitive metadata (session ID, subject, date, time,
 * participant names, status). Explicitly excludes credentials, passwords, hashes, emails,
 * and database connection handles.
 */
public class StudySessionSummary implements Serializable {

    // CONCEPT: Serialization (Version Control for Serialized Objects)
    private static final long serialVersionUID = 1L;

    private int sessionId;
    private String subject;
    private LocalDate sessionDate;
    private LocalTime sessionTime;
    private String status;
    private LocalDateTime exportedAt;
    private List<String> participantNames = new ArrayList<>();

    /**
     * CONCEPT: The transient Keyword in Java Serialization
     * WHY: Marked as transient so it is NOT serialized into the byte stream.
     * When deserialized, transient fields receive default values (null for objects, 0 for primitives)
     * and can be lazily recomputed on demand.
     */
    private transient String cachedSummaryDisplay;

    public StudySessionSummary() {
    }

    public StudySessionSummary(StudySession session, String exportedBy) {
        if (session != null) {
            this.sessionId = session.getSessionId();
            this.subject = session.getSubject();
            this.sessionDate = session.getSessionDate();
            this.sessionTime = session.getSessionTime();
            this.status = session.getStatus() != null ? session.getStatus().name() : "SCHEDULED";
            this.exportedAt = LocalDateTime.now();
            if (session.getParticipants() != null) {
                for (Student s : session.getParticipants()) {
                    this.participantNames.add(s.getName() + " (" + s.getDepartment() + ")");
                }
            }
        }
    }

    /**
     * Reconstructs or computes the display representation on demand.
     */
    public String getSummaryDisplay() {
        if (cachedSummaryDisplay == null) {
            cachedSummaryDisplay = String.format(
                "Session #%d [%s] on %s at %s. Status: %s. Participants: %s. Exported on: %s",
                sessionId,
                subject,
                HtmlUtil.formatDate(sessionDate),
                HtmlUtil.formatTime(sessionTime),
                status,
                String.join(", ", participantNames),
                HtmlUtil.formatDateTime(exportedAt)
            );
        }
        return cachedSummaryDisplay;
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

    public String getExportedAtDisplay() {
        return HtmlUtil.formatDateTime(exportedAt);
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getExportedAt() {
        return exportedAt;
    }

    public void setExportedAt(LocalDateTime exportedAt) {
        this.exportedAt = exportedAt;
    }

    public List<String> getParticipantNames() {
        return Collections.unmodifiableList(participantNames);
    }

    public void setParticipantNames(List<String> participantNames) {
        this.participantNames = participantNames != null ? new ArrayList<>(participantNames) : new ArrayList<>();
    }
}
