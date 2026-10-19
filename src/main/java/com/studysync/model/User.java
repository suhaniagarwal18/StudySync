package com.studysync.model;

import com.studysync.exception.ValidationException;
import com.studysync.util.PasswordUtil;
import com.studysync.util.ValidationUtil;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Abstract domain model representing an authenticated user in StudySync.
 * 
 * CONCEPT: Abstraction & Inheritance (Base Class)
 * WHY: Defines common attributes and contract methods for all system users (Students and Admins)
 * while delegating role-specific navigation, permissions, and dashboards to concrete subclasses.
 */
public abstract class User {

    // CONCEPT: Encapsulation (Private fields with controlled getter/setter access)
    private int userId;
    private String name;
    private String email;
    private transient String passwordHash; // Never exposed or serialized
    private String department;
    private String bio;
    private boolean active = true;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(int userId, String name, String email, String passwordHash, String department) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.department = department;
    }

    /**
     * Authenticates the user by verifying candidate plaintext password against stored BCrypt hash.
     * 
     * @param rawPassword candidate plaintext password
     * @return true if credentials match, false otherwise
     */
    public boolean login(String rawPassword) {
        if (!this.active || this.passwordHash == null) {
            return false;
        }
        return PasswordUtil.verify(rawPassword, this.passwordHash);
    }

    /**
     * Clears sensitive in-memory authentication tokens upon logout.
     */
    public void logout() {
        this.passwordHash = null;
    }

    /**
     * Updates common user profile fields with server-side validation.
     */
    public void updateProfile(String name, String department, String bio) throws ValidationException {
        ValidationUtil.validateProfileUpdate(name, department, bio);
        this.name = name.trim();
        this.department = department != null ? department.trim() : null;
        this.bio = bio != null ? bio.trim() : null;
    }

    // --------------------------------------------------------------------------
    // Abstract Methods for Runtime Polymorphism
    // --------------------------------------------------------------------------

    /**
     * Returns the user's role identifier ("STUDENT" or "ADMIN").
     * CONCEPT: Polymorphism (Dynamic Method Dispatch)
     */
    public abstract String getRole();

    /**
     * Returns the context-relative destination path for the user's dashboard.
     * CONCEPT: Polymorphism (Subclasses provide their respective entrypoint)
     */
    public abstract String getDashboardPath();

    /**
     * Returns the navigational menu items available to this specific role in the UI.
     * CONCEPT: Polymorphism (Used by common header navbar to render role-based links)
     */
    public abstract List<String> getMenuItems();

    // --------------------------------------------------------------------------
    // Getters and Setters
    // --------------------------------------------------------------------------

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Internal getter for service/DAO layer. Blanked or null in session copy.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
