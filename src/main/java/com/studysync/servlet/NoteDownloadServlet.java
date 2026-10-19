package com.studysync.servlet;

import com.studysync.exception.FileStorageException;
import com.studysync.exception.ResourceNotFoundException;
import com.studysync.filter.AuthFilter;
import com.studysync.model.Note;
import com.studysync.model.User;
import com.studysync.service.NoteService;
import com.studysync.util.FileStorageUtil;
import com.studysync.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Controller securely streaming note attachment downloads to authenticated clients.
 * 
 * CONCEPT: Secure File Streaming & HTTP Response Headers
 * WHY:
 * 1. Takes ONLY an integer note ID (never a user-controllable path or filename) to eliminate path traversal.
 * 2. Enforces authentication check before granting download access.
 * 3. Applies strict Content-Disposition, X-Content-Type-Options, and Content-Length headers.
 * 4. Polymorphically delegates binary streaming to the Note model's download(OutputStream) method.
 */
@WebServlet("/notes/download")
public class NoteDownloadServlet extends HttpServlet {

    private final NoteService noteService = new NoteService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute(AuthFilter.SESSION_USER) : null;

        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            FlashUtil.setError(req, "Note ID is required for download.");
            resp.sendRedirect(req.getContextPath() + "/notes");
            return;
        }

        try {
            int noteId = Integer.parseInt(idParam);
            Note note = noteService.getNoteById(noteId);

            String mimeType = FileStorageUtil.getMimeType(note.getOriginalFilename());
            String safeDownloadName = URLEncoder.encode(note.getDownloadName(), StandardCharsets.UTF_8).replace("+", "%20");

            resp.setContentType(mimeType);
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + note.getDownloadName() + "\"; filename*=UTF-8''" + safeDownloadName);
            resp.setHeader("X-Content-Type-Options", "nosniff");
            if (note.getFileSize() > 0) {
                resp.setContentLengthLong(note.getFileSize());
            }

            // CONCEPT: Polymorphism & Interface (Calls Downloadable.download(OutputStream))
            try (OutputStream out = resp.getOutputStream()) {
                note.download(out);
            }
        } catch (NumberFormatException nfe) {
            FlashUtil.setError(req, "Invalid note identifier.");
            resp.sendRedirect(req.getContextPath() + "/notes");
        } catch (ResourceNotFoundException rnfe) {
            FlashUtil.setError(req, rnfe.getMessage());
            resp.sendRedirect(req.getContextPath() + "/notes");
        } catch (FileStorageException fse) {
            FlashUtil.setError(req, "File unavailable: " + fse.getMessage());
            resp.sendRedirect(req.getContextPath() + "/notes");
        } catch (Exception e) {
            FlashUtil.setError(req, "Failed to download note: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/notes");
        }
    }
}
