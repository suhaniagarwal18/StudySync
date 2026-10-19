package com.studysync.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password hashing and verification utility powered by jBCrypt.
 * 
 * CONCEPT: Security & Cryptographic Password Hashing
 * WHY: Passwords must never be stored in plaintext or reversible encryption.
 * BCrypt incorporates a random 128-bit salt and an adaptive key derivation function
 * (work factor) to withstand rainbow table and brute-force attacks.
 */
public final class PasswordUtil {

    // Standard work factor (2^10 iterations) providing a balance between security and latency
    private static final int LOG_ROUNDS = 10;

    private PasswordUtil() {
        // Utility class
    }

    /**
     * Generates a secure BCrypt hash of the given plaintext password with an internal salt.
     * 
     * @param rawPassword plaintext password
     * @return salted BCrypt hash string
     */
    public static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password to hash cannot be null or empty");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Verifies whether a plaintext password matches an existing BCrypt hash.
     * 
     * @param rawPassword candidate plaintext password
     * @param hashedPassword stored BCrypt hash
     * @return true if password matches, false otherwise
     */
    public static boolean verify(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Malformed hash string
            return false;
        }
    }
}
