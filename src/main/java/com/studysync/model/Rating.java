package com.studysync.model;

import com.studysync.exception.ValidationException;
import com.studysync.util.HtmlUtil;
import com.studysync.util.ValidationUtil;
import java.time.LocalDateTime;

/**
 * Domain model representing a peer rating and feedback evaluation.
 * 
 * CONCEPT: Encapsulation & Validation
 * WHY: Enforces valid rating scales (1 to 5 stars) and non-self review invariants.
 */
public class Rating {

    private int ratingId;
    private int sessionId;
    private int reviewerId;
    private int revieweeId;
    private String reviewerName;
    private String revieweeName;
    private String sessionSubject;
    private int ratingValue;
    private String feedback;
    private LocalDateTime createdAt;

    public Rating() {
    }

    public Rating(int sessionId, int reviewerId, int revieweeId, int ratingValue, String feedback)
            throws ValidationException {
        if (reviewerId == revieweeId) {
            throw new ValidationException("You cannot submit a peer rating for yourself.");
        }
        ValidationUtil.validateRating(ratingValue, feedback);
        this.sessionId = sessionId;
        this.reviewerId = reviewerId;
        this.revieweeId = revieweeId;
        this.ratingValue = ratingValue;
        this.feedback = feedback != null ? feedback.trim() : null;
    }

    // --------------------------------------------------------------------------
    // Formatted View Getters
    // --------------------------------------------------------------------------

    public String getCreatedAtDisplay() {
        return HtmlUtil.formatDateTime(createdAt);
    }

    /**
     * Generates a visual star rating string (e.g., "★★★★☆ (4/5)").
     */
    public String getStarsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= ratingValue) {
                sb.append("★");
            } else {
                sb.append("☆");
            }
        }
        sb.append(" (").append(ratingValue).append("/5)");
        return sb.toString();
    }

    // --------------------------------------------------------------------------
    // Getters and Setters
    // --------------------------------------------------------------------------

    public int getRatingId() {
        return ratingId;
    }

    public void setRatingId(int ratingId) {
        this.ratingId = ratingId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(int reviewerId) {
        this.reviewerId = reviewerId;
    }

    public int getRevieweeId() {
        return revieweeId;
    }

    public void setRevieweeId(int revieweeId) {
        this.revieweeId = revieweeId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getRevieweeName() {
        return revieweeName;
    }

    public void setRevieweeName(String revieweeName) {
        this.revieweeName = revieweeName;
    }

    public String getSessionSubject() {
        return sessionSubject;
    }

    public void setSessionSubject(String sessionSubject) {
        this.sessionSubject = sessionSubject;
    }

    public int getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(int ratingValue) throws ValidationException {
        ValidationUtil.validateRating(ratingValue, this.feedback);
        this.ratingValue = ratingValue;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) throws ValidationException {
        ValidationUtil.validateRating(this.ratingValue > 0 ? this.ratingValue : 5, feedback);
        this.feedback = feedback != null ? feedback.trim() : null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
