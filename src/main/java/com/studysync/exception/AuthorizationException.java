package com.studysync.exception;

/**
 * Thrown when an authenticated user attempts to access or modify resources
 * belonging to another user, or accesses admin endpoints without ADMIN role.
 * 
 * CONCEPT: Access Control / Exception Handling
 */
public class AuthorizationException extends StudySyncException {

    private static final long serialVersionUID = 1L;

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
