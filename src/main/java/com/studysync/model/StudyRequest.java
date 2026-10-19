package com.studysync.model;

import com.studysync.exception.InvalidRequestException;
import com.studysync.util.HtmlUtil;
import com.studysync.util.ValidationUtil;
import java.time.LocalDateTime;

/**
 * Domain model representing a peer study request.
 * 
 * CONCEPT: State Machine & Business Invariant Enforcement
 * WHY: Enforces strict state transitions (PENDING -> ACCEPTED or REJECTED) and prevents
 * illegal states like self-invitations or modifications to finalized requests.
 */
public class StudyRequest {

    private int requestId;
    private int senderId;
    private int receiverId;
    private Student sender;
    private Student receiver;
    private String subject;
    private RequestStatus status = RequestStatus.PENDING;
    private LocalDateTime createdAt;

    public StudyRequest() {
    }

    public StudyRequest(int requestId, int senderId, int receiverId, String subject, RequestStatus status) {
        this.requestId = requestId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.subject = subject;
        this.status = status;
    }

    /**
     * Initializes a new study request, asserting business validity rules.
     */
    public void sendRequest() throws InvalidRequestException {
        if (senderId <= 0 || receiverId <= 0) {
            throw new InvalidRequestException("Sender and receiver identifiers are required.");
        }
        if (senderId == receiverId) {
            throw new InvalidRequestException("You cannot send a study request to yourself.");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new InvalidRequestException("Subject is required to initiate a study request.");
        }
        if (receiver != null && !receiver.getSkillsKnown().isEmpty()) {
            String key = ValidationUtil.toComparisonKey(subject);
            boolean knowsSubject = receiver.getSkillsKnown().stream()
                .anyMatch(s -> ValidationUtil.toComparisonKey(s).equals(key));
            if (!knowsSubject) {
                throw new InvalidRequestException("The requested student has not listed '" + subject + "' among their known skills.");
            }
        }
        this.status = RequestStatus.PENDING;
    }

    /**
     * Accepts a pending study request.
     */
    public void acceptRequest() throws InvalidRequestException {
        if (this.status != RequestStatus.PENDING) {
            throw new InvalidRequestException("Only PENDING requests can be accepted. Current status: " + status);
        }
        this.status = RequestStatus.ACCEPTED;
    }

    /**
     * Rejects a pending study request.
     */
    public void rejectRequest() throws InvalidRequestException {
        if (this.status != RequestStatus.PENDING) {
            throw new InvalidRequestException("Only PENDING requests can be rejected. Current status: " + status);
        }
        this.status = RequestStatus.REJECTED;
    }

    // --------------------------------------------------------------------------
    // Formatted View Getters
    // --------------------------------------------------------------------------

    public String getCreatedAtDisplay() {
        return HtmlUtil.formatDateTime(createdAt);
    }

    // --------------------------------------------------------------------------
    // Getters and Setters
    // --------------------------------------------------------------------------

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public Student getSender() {
        return sender;
    }

    public void setSender(Student sender) {
        this.sender = sender;
        if (sender != null) {
            this.senderId = sender.getUserId();
        }
    }

    public Student getReceiver() {
        return receiver;
    }

    public void setReceiver(Student receiver) {
        this.receiver = receiver;
        if (receiver != null) {
            this.receiverId = receiver.getUserId();
        }
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
