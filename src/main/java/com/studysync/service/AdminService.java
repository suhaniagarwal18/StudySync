package com.studysync.service;

import com.studysync.dao.NoteDAO;
import com.studysync.dao.StatsDAO;
import com.studysync.dao.UserDAO;
import com.studysync.exception.AuthorizationException;
import com.studysync.exception.ResourceNotFoundException;
import com.studysync.model.Note;
import com.studysync.model.User;
import com.studysync.util.FileStorageUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service managing administrative oversight, user account moderation, and platform analytics.
 * 
 * CONCEPT: Role-Based Authorization & Administrative Safeguards
 * WHY: Enforces critical administrative rules, including preventing an admin from inadvertently
 * deactivating their own administrative account.
 */
public class AdminService {

    private final UserDAO userDAO;
    private final NoteDAO noteDAO;
    private final StatsDAO statsDAO;

    public AdminService() {
        this.userDAO = new UserDAO();
        this.noteDAO = new NoteDAO();
        this.statsDAO = new StatsDAO();
    }

    public AdminService(UserDAO userDAO, NoteDAO noteDAO, StatsDAO statsDAO) {
        this.userDAO = userDAO;
        this.noteDAO = noteDAO;
        this.statsDAO = statsDAO;
    }

    public List<User> listAllUsers() {
        return userDAO.listAll();
    }

    /**
     * Toggles a user's active status. Admins are blocked from deactivating their own account.
     */
    public boolean toggleUserStatus(int targetUserId, int currentAdminId)
            throws ResourceNotFoundException, AuthorizationException {

        if (targetUserId == currentAdminId) {
            throw new AuthorizationException("Administrative safety rule: You cannot deactivate your own account.");
        }

        User user = userDAO.findById(targetUserId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + targetUserId);
        }

        boolean newStatus = !user.isActive();
        userDAO.setActive(targetUserId, newStatus);
        return newStatus;
    }

    public List<Note> listAllNotes() {
        return noteDAO.listAll();
    }

    /**
     * Moderates and deletes inappropriate notes.
     */
    public void deleteNoteAsAdmin(int noteId, int adminUserId)
            throws ResourceNotFoundException, AuthorizationException {

        User admin = userDAO.findById(adminUserId);
        if (admin == null || !"ADMIN".equalsIgnoreCase(admin.getRole())) {
            throw new AuthorizationException("Only platform administrators may moderate notes.");
        }

        Note note = noteDAO.findById(noteId);
        if (note == null) {
            throw new ResourceNotFoundException("Note not found with ID: " + noteId);
        }

        noteDAO.delete(noteId);
        FileStorageUtil.deleteFile(note.getFilePath());
    }

    /**
     * Gathers all system metrics for the administrative dashboard.
     */
    public Map<String, Object> getDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalUsers", statsDAO.getTotalUsersCount());
        metrics.put("totalStudents", statsDAO.getStudentCount());
        metrics.put("activeStudents", statsDAO.getActiveStudentCount());
        metrics.put("totalNotes", statsDAO.getTotalNotesCount());
        metrics.put("totalRequests", statsDAO.getTotalRequestsCount());
        metrics.put("requestsByStatus", statsDAO.getRequestsCountByStatus());
        metrics.put("totalSessions", statsDAO.getTotalSessionsCount());
        metrics.put("sessionsByStatus", statsDAO.getSessionsCountByStatus());
        metrics.put("averageRating", String.format(java.util.Locale.US, "%.1f", statsDAO.getOverallAverageRating()));
        metrics.put("topSubjects", statsDAO.getTopSubjects());
        return metrics;
    }
}
