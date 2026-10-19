package com.studysync.servlet;

import com.studysync.exception.StudySyncException;
import com.studysync.filter.AuthFilter;
import com.studysync.model.Note;
import com.studysync.model.User;
import com.studysync.service.NoteService;
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
 * Controller providing lecture notes catalog, keyword/subject search, and deletion.
 * 
 * CONCEPT: MVC (Search & Filtering)
 * WHY: Exposes multi-parameter filtering over title, module, and subject without SQL in controllers.
 */
@WebServlet("/notes")
public class NoteServlet extends HttpServlet {

    private final NoteService noteService = new NoteService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String keyword = req.getParameter("keyword");
        String subject = req.getParameter("subject");

        List<Note> notes = noteService.searchNotes(keyword, subject);
        List<String> distinctSubjects = noteService.getDistinctSubjects();

        req.setAttribute("notes", notes);
        req.setAttribute("distinctSubjects", distinctSubjects);
        req.setAttribute("keyword", keyword);
        req.setAttribute("selectedSubject", subject);

        req.setAttribute("flashSuccess", FlashUtil.getSuccess(session));
        req.setAttribute("flashError", FlashUtil.getError(session));

        req.getRequestDispatcher("/WEB-INF/views/viewNotes.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute(AuthFilter.SESSION_USER);

        String action = req.getParameter("action");
        if ("delete".equalsIgnoreCase(action)) {
            try {
                int noteId = Integer.parseInt(req.getParameter("noteId"));
                noteService.deleteNote(noteId, currentUser.getUserId());
                FlashUtil.setSuccess(req, "Note #" + noteId + " deleted successfully.");
            } catch (NumberFormatException nfe) {
                FlashUtil.setError(req, "Invalid note identifier.");
            } catch (StudySyncException se) {
                FlashUtil.setError(req, se.getMessage());
            }
        } else {
            FlashUtil.setError(req, "Unknown note action.");
        }

        resp.sendRedirect(req.getContextPath() + "/notes");
    }
}
