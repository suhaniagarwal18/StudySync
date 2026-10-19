<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Admin Dashboard - StudySync" scope="request" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-header">
    <div>
        <h1 class="page-title">Administrator Console</h1>
        <p class="page-subtitle">Platform-wide statistics, active enrollment metrics, and system analytics.</p>
    </div>
    <div style="display: flex; gap: 0.5rem;">
        <a href="<c:url value='/admin/users' />" class="btn btn-primary">Manage Users</a>
        <a href="<c:url value='/admin/notes' />" class="btn btn-secondary">Moderate Notes</a>
    </div>
</div>

<!-- Primary Stats Grid -->
<div class="card-grid">
    <div class="card stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-details">
            <h3><c:out value="${metrics.totalUsers}" /></h3>
            <p>Total Registered Users</p>
        </div>
    </div>

    <div class="card stat-card">
        <div class="stat-icon">🎓</div>
        <div class="stat-details">
            <h3><c:out value="${metrics.activeStudents}" /> / <c:out value="${metrics.totalStudents}" /></h3>
            <p>Active Students</p>
        </div>
    </div>

    <div class="card stat-card">
        <div class="stat-icon">📑</div>
        <div class="stat-details">
            <h3><c:out value="${metrics.totalNotes}" /></h3>
            <p>Uploaded Notes</p>
        </div>
    </div>

    <div class="card stat-card">
        <div class="stat-icon">⭐</div>
        <div class="stat-details">
            <h3><c:out value="${metrics.averageRating}" /> / 5.0</h3>
            <p>Global Average Rating</p>
        </div>
    </div>
</div>

<!-- Secondary Analytics Grid -->
<div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 1.5rem; margin-top: 1rem;">
    <!-- Study Requests Status Breakdown -->
    <div class="card">
        <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 1rem; color: var(--secondary);">
            Study Requests (<c:out value="${metrics.totalRequests}" />)
        </h3>
        <ul style="list-style: none;">
            <li style="display: flex; justify-content: space-between; padding: 0.5rem 0; border-bottom: 1px solid var(--border);">
                <span>Pending Review:</span>
                <strong><c:out value="${metrics.requestsByStatus['PENDING'] != null ? metrics.requestsByStatus['PENDING'] : 0}" /></strong>
            </li>
            <li style="display: flex; justify-content: space-between; padding: 0.5rem 0; border-bottom: 1px solid var(--border);">
                <span>Accepted Invitations:</span>
                <strong style="color: var(--success);"><c:out value="${metrics.requestsByStatus['ACCEPTED'] != null ? metrics.requestsByStatus['ACCEPTED'] : 0}" /></strong>
            </li>
            <li style="display: flex; justify-content: space-between; padding: 0.5rem 0;">
                <span>Declined Requests:</span>
                <strong style="color: var(--danger);"><c:out value="${metrics.requestsByStatus['REJECTED'] != null ? metrics.requestsByStatus['REJECTED'] : 0}" /></strong>
            </li>
        </ul>
    </div>

    <!-- Study Sessions Status Breakdown -->
    <div class="card">
        <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 1rem; color: var(--secondary);">
            Study Sessions (<c:out value="${metrics.totalSessions}" />)
        </h3>
        <ul style="list-style: none;">
            <li style="display: flex; justify-content: space-between; padding: 0.5rem 0; border-bottom: 1px solid var(--border);">
                <span>Scheduled / Active:</span>
                <strong style="color: var(--warning);"><c:out value="${metrics.sessionsByStatus['SCHEDULED'] != null ? metrics.sessionsByStatus['SCHEDULED'] : 0}" /></strong>
            </li>
            <li style="display: flex; justify-content: space-between; padding: 0.5rem 0; border-bottom: 1px solid var(--border);">
                <span>Successfully Completed:</span>
                <strong style="color: var(--success);"><c:out value="${metrics.sessionsByStatus['COMPLETED'] != null ? metrics.sessionsByStatus['COMPLETED'] : 0}" /></strong>
            </li>
            <li style="display: flex; justify-content: space-between; padding: 0.5rem 0;">
                <span>Cancelled Sessions:</span>
                <strong style="color: var(--danger);"><c:out value="${metrics.sessionsByStatus['CANCELLED'] != null ? metrics.sessionsByStatus['CANCELLED'] : 0}" /></strong>
            </li>
        </ul>
    </div>

    <!-- Top Subjects -->
    <div class="card">
        <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 1rem; color: var(--secondary);">
            Top Shared Subjects
        </h3>
        <ul style="list-style: none;">
            <c:forEach var="entry" items="${metrics.topSubjects}">
                <li style="display: flex; justify-content: space-between; padding: 0.5rem 0; border-bottom: 1px solid var(--border);">
                    <span><c:out value="${entry.key}" /></span>
                    <span class="badge badge-skill"><c:out value="${entry.value}" /> notes</span>
                </li>
            </c:forEach>
        </ul>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
