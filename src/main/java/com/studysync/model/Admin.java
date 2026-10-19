package com.studysync.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Concrete domain model representing a platform administrator.
 * 
 * CONCEPT: Inheritance (Specialization)
 * WHY: Specializes User with elevated administrative roles, metrics dashboards,
 * and content moderation permissions.
 * 
 * DESIGN NOTE: In StudySync's unified database schema, an Admin's adminId
 * is identical to their underlying user_id primary key in the users table.
 */
public class Admin extends User {

    private int adminId;

    public Admin() {
        super();
    }

    public Admin(int userId, String name, String email, String passwordHash, String department) {
        super(userId, name, email, passwordHash, department);
        this.adminId = userId;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    // --------------------------------------------------------------------------
    // Polymorphic Overrides
    // --------------------------------------------------------------------------

    // CONCEPT: Polymorphism (Method Overriding)
    @Override
    public String getRole() {
        return "ADMIN";
    }

    // CONCEPT: Polymorphism (Role-specific admin console entry)
    @Override
    public String getDashboardPath() {
        return "/admin/dashboard";
    }

    // CONCEPT: Polymorphism (Dynamic role-based navbar generation)
    @Override
    public List<String> getMenuItems() {
        // CONCEPT: Collections (ArrayList for ordered menu items)
        return new ArrayList<>(Arrays.asList(
            "Admin Dashboard",
            "Manage Users",
            "Manage Notes"
        ));
    }
}
