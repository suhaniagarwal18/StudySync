package com.studysync.model;

/**
 * State lifecycle of a scheduled study session.
 * 
 * CONCEPT: Enums & State Management
 * Valid transitions: SCHEDULED -> COMPLETED or SCHEDULED -> CANCELLED.
 */
public enum SessionStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED;

    public boolean canCancel() {
        return this == SCHEDULED;
    }

    public boolean canComplete() {
        return this == SCHEDULED;
    }
}
