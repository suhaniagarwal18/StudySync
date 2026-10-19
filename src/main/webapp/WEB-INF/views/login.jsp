<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Log In - StudySync" scope="request" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="form-container">
    <div style="text-align: center; margin-bottom: 1.5rem;">
        <h2 style="font-size: 1.5rem; font-weight: 700; color: var(--secondary);">Welcome to StudySync</h2>
        <p style="color: var(--text-muted); font-size: 0.9rem;">Sign in with your academic university credentials</p>
    </div>

    <form action="<c:url value='/login' />" method="post">
        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />

        <div class="form-group">
            <label class="form-label" for="email">College Email Address</label>
            <input type="email" id="email" name="email" class="form-control"
                   value="<c:out value='${email != null ? email : param.email}' />" required autofocus />
        </div>

        <div class="form-group">
            <label class="form-label" for="password">Password</label>
            <input type="password" id="password" name="password" class="form-control" required />
        </div>

        <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 0.5rem;">
            Sign In →
        </button>
    </form>

    <div style="text-align: center; margin-top: 1.5rem; font-size: 0.9rem; color: var(--text-muted);">
        Don't have an account yet? <a href="<c:url value='/register' />" style="color: var(--primary); font-weight: 600;">Register as Student</a>
    </div>

    <!-- Quick Demo Credentials Box for Evaluation -->
    <div style="margin-top: 2rem; background: #f8fafc; border: 1px dashed #cbd5e1; border-radius: var(--radius); padding: 1rem; font-size: 0.85rem;">
        <p style="font-weight: 700; color: var(--secondary); margin-bottom: 0.35rem;">🔑 University Demo Credentials:</p>
        <p style="margin-bottom: 0.25rem;"><strong>Admin:</strong> <code>admin@studysync.edu</code> | <code>Admin@123</code></p>
        <p style="margin-bottom: 0.25rem;"><strong>Student A (CS):</strong> <code>aarav@studysync.edu</code> | <code>Student@123</code></p>
        <p><strong>Student B (IT):</strong> <code>diya@studysync.edu</code> | <code>Student@123</code></p>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
