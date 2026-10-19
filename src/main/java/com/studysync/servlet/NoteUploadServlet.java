package com.studysync.servlet;

import com.studysync.exception.StudySyncException;
import com.studysync.filter.AuthFilter;
import com.studysync.model.Note;
import com.studysync.model.User;
import com.studysync.service.NoteService;
import com.studysync.service.ProfileService;
import com.studysync.util.CsrfUtil;
import com.studysync.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Controller handling multipart file uploads for academic study resources.
 * 
 * CONCEPT: Jakarta Servlet 6.0 @MultipartConfig & Safe File Ingestion
 * WHY:
 * 1. Enables standard multipart request processing with a 10 MB maximum file threshold.
 * 2. Parses the CSRF token directly from multipart parts before initiating file persistence.
 * 3. Extracts client metadata safely and delegates physical storage to NoteService and FileStorageUtil.
 */
@WebServlet("/notes/upload")
@MultipartConfig(
    maxFileSize = 10485760L,      // 10 MB max file size
    maxRequestSize = 20971520L,   // 20 MB max total request size
    fileSizeThreshold = 524288    // 512 KB in-memory buffer threshold
)
public class NoteUploadServlet extends HttpServlet {

    private final NoteService noteService = new NoteService();
    private final ProfileService profileService = new ProfileService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        req.setAttribute("allSkills", profileService.getAllSkills());
        req.setAttribute("flashSuccess", FlashUtil.getSuccess(session));
        req.setAttribute("flashError", FlashUtil.getError(session));
        req.getRequestDispatcher("/WEB-INF/views/uploadNotes.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute(AuthFilter.SESSION_USER);

        // Verify CSRF token for multipart upload
        String sessionToken = (String) session.getAttribute(CsrfUtil.CSRF_SESSION_ATTR);
        String candidateToken = req.getParameter(CsrfUtil.CSRF_PARAM_NAME);

        if (candidateToken == null || candidateToken.isBlank()) {
            Part csrfPart = req.getPart(CsrfUtil.CSRF_PARAM_NAME);
            if (csrfPart != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(csrfPart.getInputStream(), StandardCharsets.UTF_8))) {
                    candidateToken = reader.readLine();
                }
            }
        }

        if (!CsrfUtil.constantTimeEquals(sessionToken, candidateToken)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }

        String title = req.getParameter("title");
        String subject = req.getParameter("subject");
        String module = req.getParameter("module");

        Part filePart = req.getPart("file");
        if (filePart == null || filePart.getSize() <= 0) {
            req.setAttribute("error", "Please select a non-empty document or image to upload.");
            req.setAttribute("title", title);
            req.setAttribute("subject", subject);
            req.setAttribute("module", module);
            req.setAttribute("allSkills", profileService.getAllSkills());
            req.getRequestDispatcher("/WEB-INF/views/uploadNotes.jsp").forward(req, resp);
            return;
        }

        String clientFilename = filePart.getSubmittedFileName();
        long fileSize = filePart.getSize();

        try (InputStream in = filePart.getInputStream()) {
            Note created = noteService.uploadNote(
                currentUser.getUserId(),
                title,
                subject,
                module,
                clientFilename,
                in,
                fileSize
            );
            FlashUtil.setSuccess(req, "Note '" + created.getTitle() + "' uploaded successfully!");
            resp.sendRedirect(req.getContextPath() + "/notes");
        } catch (StudySyncException se) {
            req.setAttribute("error", se.getMessage());
            req.setAttribute("title", title);
            req.setAttribute("subject", subject);
            req.setAttribute("module", module);
            req.setAttribute("allSkills", profileService.getAllSkills());
            req.getRequestDispatcher("/WEB-INF/views/uploadNotes.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("error", "Upload failed: " + e.getMessage());
            req.setAttribute("title", title);
            req.setAttribute("subject", subject);
            req.setAttribute("module", module);
            req.setAttribute("allSkills", profileService.getAllSkills());
            req.getRequestDispatcher("/WEB-INF/views/uploadNotes.jsp").forward(req, resp);
        }
    }
}
