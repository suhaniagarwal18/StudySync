<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="404 - Page Not Found" scope="request" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="card empty-state" style="max-width: 550px; margin: 3rem auto;">
    <div class="empty-state-icon" style="color: var(--accent);">🔍</div>
    <h1 style="font-size: 1.75rem; font-weight: 700; color: var(--secondary); margin-bottom: 0.5rem;">
        404 - Resource Not Found
    </h1>
    <p style="color: var(--text-muted); margin-bottom: 1.5rem;">
        The study note, session, request, or page you requested could not be located on the server.
    </p>
    <a href="<c:url value='/' />" class="btn btn-primary">Return to Homepage</a>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
