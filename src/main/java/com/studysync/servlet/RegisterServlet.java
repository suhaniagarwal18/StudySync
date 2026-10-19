package com.studysync.servlet;

import com.studysync.exception.StudySyncException;
import com.studysync.exception.ValidationException;
import com.studysync.model.Student;
import com.studysync.service.AuthService;
import com.studysync.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controller managing student account self-registration.
 * 
 * CONCEPT: MVC (Controller Processing & PRG Pattern)
 * WHY: Validates student registration data server-side and uses the Post-Redirect-Get
 * pattern upon creation to prevent duplicate submission on page refresh.
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("flashSuccess", FlashUtil.getSuccess(req.getSession(false)));
        req.setAttribute("flashError", FlashUtil.getError(req.getSession(false)));
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String department = req.getParameter("department");

        try {
            Student student = authService.registerStudent(name, email, password, department);
            FlashUtil.setSuccess(req, "Registration successful! You can now log in with your credentials.");
            resp.sendRedirect(req.getContextPath() + "/login");
        } catch (ValidationException ve) {
            req.setAttribute("error", ve.getMessage());
            req.setAttribute("fieldErrors", ve.getFieldErrors());
            req.setAttribute("name", name);
            req.setAttribute("email", email);
            req.setAttribute("department", department);
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
        } catch (StudySyncException se) {
            req.setAttribute("error", se.getMessage());
            req.setAttribute("name", name);
            req.setAttribute("email", email);
            req.setAttribute("department", department);
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
        }
    }
}
