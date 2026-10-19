package com.studysync.exception;

/**
 * Base checked exception for all business and application logic errors in StudySync.
 * 
 * CONCEPT: Exception Handling (Custom Exception Hierarchy)
 * WHY: Inheriting from java.lang.Exception creates a checked exception that forces
 * calling controllers/servlets to explicitly handle recoverable business errors
 * (e.g., displaying friendly flash messages to the user) rather than crashing the request.
 */
public class StudySyncException extends Exception {

    private static final long serialVersionUID = 1L;

    public StudySyncException(String message) {
        super(message);
    }

    public StudySyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
