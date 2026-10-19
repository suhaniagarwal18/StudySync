package com.studysync.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests verifying BCrypt password hashing and verification invariants.
 */
class PasswordUtilTest {

    @Test
    @DisplayName("Hash generation should produce salted BCrypt hashes and verify correct credentials")
    void testHashAndVerifyPositive() {
        String raw = "Student@123";
        String hash = PasswordUtil.hash(raw);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$10$") || hash.startsWith("$2b$10$"));
        assertNotEquals(raw, hash);

        assertTrue(PasswordUtil.verify(raw, hash), "PasswordUtil should verify the correct password");
    }

    @Test
    @DisplayName("Verification should fail for incorrect candidate passwords")
    void testVerifyNegative() {
        String hash = PasswordUtil.hash("Admin@123");

        assertFalse(PasswordUtil.verify("WrongPass@123", hash));
        assertFalse(PasswordUtil.verify("", hash));
        assertFalse(PasswordUtil.verify(null, hash));
        assertFalse(PasswordUtil.verify("Admin@123", "malformed_hash_string"));
    }

    @Test
    @DisplayName("Hashing null or empty input should throw IllegalArgumentException")
    void testHashInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(""));
    }
}
