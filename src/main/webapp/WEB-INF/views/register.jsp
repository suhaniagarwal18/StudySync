<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Register Student Account - StudySync" scope="request" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="form-container">
    <div style="text-align: center; margin-bottom: 1.5rem;">
        <h2 style="font-size: 1.5rem; font-weight: 700; color: var(--secondary);">Join StudySync</h2>
        <p style="color: var(--text-muted); font-size: 0.9rem;">Create your university peer study buddy account</p>
    </div>

    <form action="<c:url value='/register' />" method="post">
        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />

        <div class="form-group">
            <label class="form-label" for="name">Full Name</label>
            <input type="text" id="name" name="name" class="form-control"
                   value="<c:out value='${name != null ? name : param.name}' />" required placeholder="e.g. Aarav Sharma" />
            <c:if test="${not empty fieldErrors['name']}">
                <div class="field-error"><c:out value="${fieldErrors['name']}" /></div>
            </c:if>
        </div>

        <div class="form-group">
            <label class="form-label" for="email">College Email Address</label>
            <input type="email" id="email" name="email" class="form-control"
                   value="<c:out value='${email != null ? email : param.email}' />" required placeholder="e.g. student@university.edu" />
            <c:if test="${not empty fieldErrors['email']}">
                <div class="field-error"><c:out value="${fieldErrors['email']}" /></div>
            </c:if>
        </div>

        <div class="form-group">
            <label class="form-label" for="department">Academic Department</label>
            <select id="department" name="department" class="form-control" required>
                <option value="">-- Select Department --</option>
                <option value="Computer Science" ${department == 'Computer Science' ? 'selected' : ''}>Computer Science</option>
                <option value="Information Technology" ${department == 'Information Technology' ? 'selected' : ''}>Information Technology</option>
                <option value="Electronics & Communication" ${department == 'Electronics & Communication' ? 'selected' : ''}>Electronics & Communication</option>
                <option value="Data Science" ${department == 'Data Science' ? 'selected' : ''}>Data Science</option>
                <option value="AI & Machine Learning" ${department == 'AI & Machine Learning' ? 'selected' : ''}>AI & Machine Learning</option>
                <option value="Software Engineering" ${department == 'Software Engineering' ? 'selected' : ''}>Software Engineering</option>
                <option value="Mechanical Engineering" ${department == 'Mechanical Engineering' ? 'selected' : ''}>Mechanical Engineering</option>
            </select>
            <c:if test="${not empty fieldErrors['department']}">
                <div class="field-error"><c:out value="${fieldErrors['department']}" /></div>
            </c:if>
        </div>

        <div class="form-group">
            <label class="form-label" for="password">Create Password</label>
            <input type="password" id="password" name="password" class="form-control" required
                   placeholder="Min 8 characters, at least 1 letter & 1 number" />
            <span style="font-size: 0.75rem; color: var(--text-muted);">Must be at least 8 characters with letters and numbers.</span>
            <c:if test="${not empty fieldErrors['password']}">
                <div class="field-error"><c:out value="${fieldErrors['password']}" /></div>
            </c:if>
        </div>

        <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 0.5rem;">
            Create Student Account →
        </button>
    </form>

    <div style="text-align: center; margin-top: 1.5rem; font-size: 0.9rem; color: var(--text-muted);">
        Already have an account? <a href="<c:url value='/login' />" style="color: var(--primary); font-weight: 600;">Sign In</a>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
