package com.studysync.service;

import com.studysync.dao.UserDAO;
import com.studysync.exception.AuthenticationException;
import com.studysync.exception.ValidationException;
import com.studysync.model.Student;
import com.studysync.model.User;
import com.studysync.util.PasswordUtil;
import com.studysync.util.ValidationUtil;

/**
 * Service orchestrating user registration, authentication, and credential validation.
 * 
 * CONCEPT: Service Layer (Separation of Concerns)
 * WHY: Contains business authentication rules and transactional coordination, keeping servlets
 * lightweight and free from database queries or hashing algorithms.
 */
public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Registers a new student account after checking email uniqueness and validating input.
     * Note: Admins cannot be self-registered; they are provisioned via administrative seeds.
     */
    public Student registerStudent(String name, String email, String password, String department)
            throws ValidationException, AuthenticationException {

        ValidationUtil.validateRegistration(name, email, password, department);

        String normalizedEmail = email.trim().toLowerCase();
        if (userDAO.existsByEmail(normalizedEmail)) {
            throw new ValidationException("An account with email '" + normalizedEmail + "' already exists.");
        }

        String hashedPassword = PasswordUtil.hash(password);
        Student student = new Student();
        student.setName(name.trim());
        student.setEmail(normalizedEmail);
        student.setPasswordHash(hashedPassword);
        student.setDepartment(department.trim());
        student.setActive(true);

        User created = userDAO.create(student);
        return (Student) created;
    }

    /**
     * Authenticates a user (Student or Admin).
     * 
     * @param email user email
     * @param rawPassword candidate plaintext password
     * @return authenticated User model
     * @throws AuthenticationException with a generic error message on failure
     */
    public User authenticate(String email, String rawPassword) throws AuthenticationException {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isEmpty()) {
            throw new AuthenticationException("Invalid email or password.");
        }

        User user = userDAO.findByEmail(email.trim());
        if (user == null) {
            // Generic message prevents username enumeration attacks
            throw new AuthenticationException("Invalid email or password.");
        }

        if (!user.isActive()) {
            throw new AuthenticationException("This account has been deactivated. Please contact the administrator.");
        }

        // CONCEPT: Polymorphism (Delegates credential check to User model's login method)
        if (!user.login(rawPassword)) {
            throw new AuthenticationException("Invalid email or password.");
        }

        // Hydrate skills if this user is a student
        if (user instanceof Student student) {
            return userDAO.loadStudentWithSkills(student.getUserId());
        }

        return user;
    }
}
