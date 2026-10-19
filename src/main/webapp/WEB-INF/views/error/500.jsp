<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="500 - Server Error" scope="request" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="card empty-state" style="max-width: 550px; margin: 3rem auto;">
    <div class="empty-state-icon" style="color: var(--danger);">⚠️</div>
    <h1 style="font-size: 1.75rem; font-weight: 700; color: var(--secondary); margin-bottom: 0.5rem;">
        500 - System Error
    </h1>
    <p style="color: var(--text-muted); margin-bottom: 1.5rem;">
        An unexpected application error occurred while processing your request. The issue has been logged securely for system administrators.
    </p>
    <a href="<c:url value='/' />" class="btn btn-primary">Return to Homepage</a>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
