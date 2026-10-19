package com.studysync.util;

/**
 * Utility main class for generating authentic BCrypt password hashes for database seeding.
 * 
 * CONCEPT: Password Hashing & Seed Data Preparation
 */
public class HashGenerator {

    public static void main(String[] args) {
        String adminPassword = "Admin@123";
        String studentPassword = "Student@123";

        String adminHash = PasswordUtil.hash(adminPassword);
        String studentHash = PasswordUtil.hash(studentPassword);

        System.out.println("ADMIN_HASH=" + adminHash);
        System.out.println("STUDENT_HASH=" + studentHash);

        System.out.println("Admin verify: " + PasswordUtil.verify(adminPassword, adminHash));
        System.out.println("Student verify: " + PasswordUtil.verify(studentPassword, studentHash));
    }
}
