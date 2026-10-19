package com.studysync.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thrown when user input fails server-side validation rules.
 * Carries field-specific error messages to support granular form validation in views.
 * 
 * CONCEPT: Encapsulation & Exception Handling
 */
public class ValidationException extends StudySyncException {

    private static final long serialVersionUID = 1L;
    
    // CONCEPT: Collections (LinkedHashMap preserves order of form validation errors)
    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message);
        this.fieldErrors = Collections.emptyMap();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors != null ? new LinkedHashMap<>(fieldErrors) : Collections.emptyMap();
    }

    public Map<String, String> getFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
}
