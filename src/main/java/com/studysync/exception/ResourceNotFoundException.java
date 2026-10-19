package com.studysync.exception;

/**
 * Thrown when an expected domain entity (User, Note, StudyRequest, StudySession)
 * cannot be found in the database.
 * 
 * CONCEPT: Resource Lifecycle & 404 Exception Mapping
 */
public class ResourceNotFoundException extends StudySyncException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
