package com.studysync.exception;

/**
 * Thrown when user authentication fails due to invalid credentials or an inactive account.
 * 
 * CONCEPT: Exception Handling (Domain-Specific Security Exception)
 */
public class AuthenticationException extends StudySyncException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
