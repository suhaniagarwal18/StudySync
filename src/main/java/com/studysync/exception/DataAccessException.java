package com.studysync.exception;

/**
 * Unchecked runtime exception wrapping low-level JDBC SQLExceptions.
 * 
 * CONCEPT: Exception Wrapping (Checked to Unchecked Translation)
 * WHY: Translates fatal database/connectivity SQLExceptions into an unchecked runtime
 * exception. This prevents DAOs from leaking SQL details to servlets and views while
 * allowing fatal errors to bubble up to error handlers without cluttering business method signatures.
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
