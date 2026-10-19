package com.studysync.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Flash message utility supporting the Post-Redirect-Get (PRG) pattern.
 * 
 * CONCEPT: Web Architecture (PRG Pattern & Session Flash Attributes)
 * WHY: Stores one-time feedback messages (success/error) in the user's HttpSession
 * across a redirect, and consumes (removes) them immediately upon read so they do not
 * persist across page refreshes or browser back navigation.
 */
public final class FlashUtil {

    public static final String FLASH_SUCCESS = "flashSuccess";
    public static final String FLASH_ERROR = "flashError";

    private FlashUtil() {
        // Utility class
    }

    public static void setSuccess(HttpServletRequest request, String message) {
        HttpSession session = request.getSession(true);
        session.setAttribute(FLASH_SUCCESS, message);
    }

    public static void setError(HttpServletRequest request, String message) {
        HttpSession session = request.getSession(true);
        session.setAttribute(FLASH_ERROR, message);
    }

    /**
     * Extracts and removes the success message from the session.
     */
    public static String getSuccess(HttpSession session) {
        if (session == null) {
            return null;
        }
        String msg = (String) session.getAttribute(FLASH_SUCCESS);
        session.removeAttribute(FLASH_SUCCESS);
        return msg;
    }

    /**
     * Extracts and removes the error message from the session.
     */
    public static String getError(HttpSession session) {
        if (session == null) {
            return null;
        }
        String msg = (String) session.getAttribute(FLASH_ERROR);
        session.removeAttribute(FLASH_ERROR);
        return msg;
    }
}
