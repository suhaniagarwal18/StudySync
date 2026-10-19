package com.studysync.servlet;

import com.studysync.exception.StudySyncException;
import com.studysync.filter.AuthFilter;
import com.studysync.model.Note;
import com.studysync.model.User;
import com.studysync.service.AdminService;
import com.studysync.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Controller allowing administrators to audit all uploaded materials and delete inappropriate content.
 * 
 * CONCEPT: Admin Content Moderation
 * WHY: Provides full system oversight over peer-shared notes with file and metadata purging.
 */
@WebServlet("/admin/notes")
public class AdminNoteServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        List<Note> notes = adminService.listAllNotes();
        req.setAttribute("notes", notes);
        req.setAttribute("flashSuccess", FlashUtil.getSuccess(session));
        req.setAttribute("flashError", FlashUtil.getError(session));
        req.getRequestDispatcher("/WEB-INF/views/admin/adminNotes.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentAdmin = (User) session.getAttribute(AuthFilter.SESSION_USER);

        String action = req.getParameter("action");
        if ("delete".equalsIgnoreCase(action)) {
            try {
                int noteId = Integer.parseInt(req.getParameter("noteId"));
                adminService.deleteNoteAsAdmin(noteId, currentAdmin.getUserId());
                FlashUtil.setSuccess(req, "Note #" + noteId + " successfully removed by administrator.");
            } catch (NumberFormatException nfe) {
                FlashUtil.setError(req, "Invalid note identifier.");
            } catch (StudySyncException se) {
                FlashUtil.setError(req, se.getMessage());
            }
        } else {
            FlashUtil.setError(req, "Unknown note moderation action.");
        }

        resp.sendRedirect(req.getContextPath() + "/admin/notes");
    }
}
