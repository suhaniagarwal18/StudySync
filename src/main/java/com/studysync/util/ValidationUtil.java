package com.studysync.util;

import com.studysync.exception.ValidationException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Server-side input validation and sanitization utility.
 * 
 * CONCEPT: Input Validation & Defensive Programming
 * WHY: Never trust client-supplied input. All input must be validated on the server
 * before touching the database or triggering business operations.
 */
public final class ValidationUtil {

    // RFC 5322 compliant simplified email regex
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // Password must be at least 8 characters and contain at least 1 letter and 1 digit
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[A-Za-z])(?=.*\\d).{8,}$"
    );

    // Skill name allowed: alphanumeric, spaces, plus/hash (e.g., C++, C#), hyphen
    private static final Pattern SKILL_NAME_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+#.-][A-Za-z0-9+#.\\s-]{0,78}[A-Za-z0-9+#.-]$"
    );

    private ValidationUtil() {
        // Utility class
    }

    /**
     * Trims a string and returns null if empty or null.
     */
    public static String trim(String input) {
        return input == null ? null : input.trim();
    }

    /**
     * Normalizes a skill or subject name: trimmed and converted to canonical Title/clean form.
     */
    public static String normalizeSkillName(String skillName) {
        if (skillName == null) {
            return null;
        }
        String trimmed = skillName.trim();
        // Replace multiple whitespace with single space
        return trimmed.replaceAll("\\s+", " ");
    }

    /**
     * Canonical key for comparison (lower case trimmed).
     */
    public static String toComparisonKey(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    /**
     * Validates student registration inputs.
     */
    public static void validateRegistration(String name, String email, String password, String department)
            throws ValidationException {
        Map<String, String> errors = new LinkedHashMap<>();

        if (name == null || name.trim().length() < 2 || name.trim().length() > 100) {
            errors.put("name", "Name must be between 2 and 100 characters.");
        }

        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches() || email.trim().length() > 150) {
            errors.put("email", "Please provide a valid college email address (up to 150 characters).");
        }

        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            errors.put("password", "Password must be at least 8 characters and include at least one letter and one number.");
        }

        if (department == null || department.trim().isEmpty() || department.trim().length() > 100) {
            errors.put("department", "Department cannot be empty and must be under 100 characters.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Registration validation failed.", errors);
        }
    }

    /**
     * Validates student profile updates.
     */
    public static void validateProfileUpdate(String name, String department, String bio) throws ValidationException {
        Map<String, String> errors = new LinkedHashMap<>();

        if (name == null || name.trim().length() < 2 || name.trim().length() > 100) {
            errors.put("name", "Name must be between 2 and 100 characters.");
        }

        if (department == null || department.trim().isEmpty() || department.trim().length() > 100) {
            errors.put("department", "Department cannot be empty and must be under 100 characters.");
        }

        if (bio != null && bio.length() > 500) {
            errors.put("bio", "Bio must not exceed 500 characters.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Profile update validation failed.", errors);
        }
    }

    /**
     * Validates a skill name.
     */
    public static void validateSkillName(String skillName) throws ValidationException {
        if (skillName == null || skillName.trim().length() < 2 || skillName.trim().length() > 80) {
            throw new ValidationException("Skill name must be between 2 and 80 characters.");
        }
        if (!SKILL_NAME_PATTERN.matcher(skillName.trim()).matches()) {
            throw new ValidationException("Skill name contains invalid characters. Only letters, digits, and symbols (+, #, -, .) are permitted.");
        }
    }

    /**
     * Validates subject name.
     */
    public static void validateSubject(String subject) throws ValidationException {
        if (subject == null || subject.trim().length() < 2 || subject.trim().length() > 80) {
            throw new ValidationException("Subject must be between 2 and 80 characters.");
        }
    }

    /**
     * Validates a rating value (1-5) and feedback.
     */
    public static void validateRating(int ratingValue, String feedback) throws ValidationException {
        if (ratingValue < 1 || ratingValue > 5) {
            throw new ValidationException("Rating value must be an integer between 1 and 5.");
        }
        if (feedback != null && feedback.length() > 500) {
            throw new ValidationException("Feedback text must not exceed 500 characters.");
        }
    }

    /**
     * Validates that a session date is today or in the future.
     */
    public static void validateFutureOrPresentDate(LocalDate date) throws ValidationException {
        if (date == null) {
            throw new ValidationException("Session date is required.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new ValidationException("Session date cannot be scheduled in the past.");
        }
    }
}
