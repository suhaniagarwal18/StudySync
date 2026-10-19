package com.studysync.exception;

/**
 * Thrown when an operation violates study request, study session, or rating business rules
 * (e.g., self-request, scheduling past dates, rating uncompleted sessions).
 * 
 * CONCEPT: Business Invariant Enforcement
 */
public class InvalidRequestException extends StudySyncException {

    private static final long serialVersionUID = 1L;

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
