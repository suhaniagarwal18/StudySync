package com.studysync.model;

/**
 * State lifecycle of a peer study request.
 * 
 * CONCEPT: Enums & State Management
 * Valid transitions: PENDING -> ACCEPTED or PENDING -> REJECTED.
 */
public enum RequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED;

    public boolean isTerminal() {
        return this == ACCEPTED || this == REJECTED;
    }
}
