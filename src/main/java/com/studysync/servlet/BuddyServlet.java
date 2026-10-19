package com.studysync.servlet;

import com.studysync.filter.AuthFilter;
import com.studysync.model.MatchResult;
import com.studysync.model.Student;
import com.studysync.model.User;
import com.studysync.service.MatchingService;
import com.studysync.service.ProfileService;
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
 * Controller executing peer study buddy matching and subject-specific peer searches.
 * 
 * CONCEPT: MVC (Controller Calling Domain Service Algorithm)
 * WHY: Invokes MatchingService to calculate set-intersection matches for needed skills,
 * and facilitates targeted search of students who possess specific subject expertise.
 */
@WebServlet("/buddies")
public class BuddyServlet extends HttpServlet {

    private final MatchingService matchingService = new MatchingService();
    private final ProfileService profileService = new ProfileService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute(AuthFilter.SESSION_USER);

        String subjectQuery = req.getParameter("subject");
        List<MatchResult> matches;

        try {
            User profile = profileService.getUserProfile(currentUser.getUserId());
            if (profile instanceof Student student) {
                req.setAttribute("student", student);

                if (subjectQuery != null && !subjectQuery.isBlank()) {
                    matches = matchingService.searchStudentsBySubject(currentUser.getUserId(), subjectQuery);
                    req.setAttribute("searchSubject", subjectQuery.trim());
                    req.setAttribute("isSubjectSearch", true);
                } else {
                    matches = matchingService.findMatchesForStudent(student);
                    req.setAttribute("isSubjectSearch", false);
                }

                req.setAttribute("matches", matches);
            }

            req.setAttribute("allSkills", profileService.getAllSkills());
            req.setAttribute("flashSuccess", FlashUtil.getSuccess(session));
            req.setAttribute("flashError", FlashUtil.getError(session));

            req.getRequestDispatcher("/WEB-INF/views/findBuddy.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("error", "Error searching for study buddies: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/findBuddy.jsp").forward(req, resp);
        }
    }
}
