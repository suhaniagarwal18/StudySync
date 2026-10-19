package com.studysync.util;

import com.studysync.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests verifying input validation rules and error detection.
 */
class ValidationUtilTest {

    @Test
    @DisplayName("Valid registration details should pass validation")
    void testValidRegistration() {
        assertDoesNotThrow(() -> ValidationUtil.validateRegistration(
            "Aarav Sharma",
            "aarav@studysync.edu",
            "Student@123",
            "Computer Science"
        ));
    }

    @Test
    @DisplayName("Invalid email formats should fail validation")
    void testInvalidEmails() {
        assertThrows(ValidationException.class, () -> ValidationUtil.validateRegistration("Test", "invalid-email", "Pass1234", "CS"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateRegistration("Test", "test@", "Pass1234", "CS"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateRegistration("Test", "@example.com", "Pass1234", "CS"));
    }

    @Test
    @DisplayName("Weak passwords (too short, missing letters, or missing digits) should fail validation")
    void testWeakPasswords() {
        // Less than 8 chars
        assertThrows(ValidationException.class, () -> ValidationUtil.validateRegistration("Test", "test@edu.com", "P123", "CS"));
        // Only letters
        assertThrows(ValidationException.class, () -> ValidationUtil.validateRegistration("Test", "test@edu.com", "PasswordOnly", "CS"));
        // Only numbers
        assertThrows(ValidationException.class, () -> ValidationUtil.validateRegistration("Test", "test@edu.com", "12345678", "CS"));
    }

    @Test
    @DisplayName("Valid skill names (including C++, C#, trimmed) should pass")
    void testValidSkills() {
        assertDoesNotThrow(() -> ValidationUtil.validateSkillName("Java"));
        assertDoesNotThrow(() -> ValidationUtil.validateSkillName("C++"));
        assertDoesNotThrow(() -> ValidationUtil.validateSkillName("C#"));
        assertDoesNotThrow(() -> ValidationUtil.validateSkillName("Operating Systems"));
    }

    @Test
    @DisplayName("Invalid skill names (too short, empty, or malicious characters) should fail")
    void testInvalidSkills() {
        assertThrows(ValidationException.class, () -> ValidationUtil.validateSkillName(""));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateSkillName("A"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateSkillName("<script>alert(1)</script>"));
    }

    @Test
    @DisplayName("Rating range validation: 1 to 5 valid; 0 and 6 rejected")
    void testRatingRange() {
        assertDoesNotThrow(() -> ValidationUtil.validateRating(1, "Acceptable"));
        assertDoesNotThrow(() -> ValidationUtil.validateRating(3, "Average"));
        assertDoesNotThrow(() -> ValidationUtil.validateRating(5, "Outstanding"));

        assertThrows(ValidationException.class, () -> ValidationUtil.validateRating(0, "Invalid 0"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateRating(6, "Invalid 6"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateRating(-1, "Negative"));
    }

    @Test
    @DisplayName("Session date validation: today or future valid; past dates rejected")
    void testDateValidation() {
        assertDoesNotThrow(() -> ValidationUtil.validateFutureOrPresentDate(LocalDate.now()));
        assertDoesNotThrow(() -> ValidationUtil.validateFutureOrPresentDate(LocalDate.now().plusDays(3)));

        assertThrows(ValidationException.class, () -> ValidationUtil.validateFutureOrPresentDate(LocalDate.now().minusDays(1)));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateFutureOrPresentDate(null));
    }

    @Test
    @DisplayName("Canonical key normalization preserves semantic equality")
    void testNormalization() {
        assertEquals("java", ValidationUtil.toComparisonKey("  Java  "));
        assertEquals("operating systems", ValidationUtil.toComparisonKey("Operating   Systems"));
    }
}
