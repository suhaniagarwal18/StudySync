package com.studysync.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cross-Site Request Forgery (CSRF) token generation and validation utility.
 * 
 * CONCEPT: Web Application Security (CSRF Defense)
 * WHY: Protects all state-changing HTTP POST operations from forged requests.
 * Uses cryptographically secure random byte generation and constant-time comparison
 * (MessageDigest.isEqual) to prevent timing side-channel attacks.
 */
public final class CsrfUtil {

    public static final String CSRF_SESSION_ATTR = "csrfToken";
    public static final String CSRF_PARAM_NAME = "csrfToken";
    public static final String CSRF_HEADER_NAME = "X-CSRF-TOKEN";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CsrfUtil() {
        // Utility class
    }

    /**
     * Retrieves the existing CSRF token for the HTTP session or creates a new one.
     * 
     * @param session the user's HttpSession
     * @return 32-byte URL-safe base64 token string
     */
    public static String getOrCreateToken(HttpSession session) {
        if (session == null) {
            return generateNewToken();
        }
        String token = (String) session.getAttribute(CSRF_SESSION_ATTR);
        if (token == null || token.isBlank()) {
            token = generateNewToken();
            session.setAttribute(CSRF_SESSION_ATTR, token);
        }
        return token;
    }

    /**
     * Generates a cryptographically strong 32-byte URL-safe Base64 token.
     */
    public static String generateNewToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Rotates the token, storing a fresh token in the session.
     */
    public static String refreshToken(HttpSession session) {
        String newToken = generateNewToken();
        if (session != null) {
            session.setAttribute(CSRF_SESSION_ATTR, newToken);
        }
        return newToken;
    }

    /**
     * Validates an incoming request token against the session token using constant-time comparison.
     * 
     * @param request the incoming HTTP request
     * @return true if candidate token matches session token exactly
     */
    public static boolean isValidToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String sessionToken = (String) session.getAttribute(CSRF_SESSION_ATTR);
        if (sessionToken == null || sessionToken.isBlank()) {
            return false;
        }

        // Check request parameter first, then header fallback
        String requestToken = request.getParameter(CSRF_PARAM_NAME);
        if (requestToken == null || requestToken.isBlank()) {
            requestToken = request.getHeader(CSRF_HEADER_NAME);
        }

        return constantTimeEquals(sessionToken, requestToken);
    }

    /**
     * Performs a constant-time equality check to thwart timing side-channel attacks.
     */
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(aBytes, bBytes);
    }
}
