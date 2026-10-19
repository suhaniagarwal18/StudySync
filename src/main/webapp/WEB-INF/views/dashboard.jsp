<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Student Dashboard - StudySync" scope="request" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-header">
    <div>
        <h1 class="page-title">Welcome back, <c:out value="${student.name}" />!</h1>
        <p class="page-subtitle"><c:out value="${student.department}" /> &bull; <c:out value="${student.email}" /></p>
    </div>
    <div style="display: flex; gap: 0.5rem;">
        <a href="<c:url value='/buddies' />" class="btn btn-primary">🤝 Find Study Buddies</a>
        <a href="<c:url value='/notes/upload' />" class="btn btn-secondary">📤 Upload Notes</a>
    </div>
</div>

<!-- Metric Cards Grid -->
<div class="card-grid">
    <div class="card stat-card">
        <div class="stat-icon">📬</div>
        <div class="stat-details">
            <h3><c:out value="${pendingRequestsCount}" /></h3>
            <p>Pending Requests</p>
            <a href="<c:url value='/requests' />" style="font-size: 0.8rem; color: var(--primary);">View Requests →</a>
        </div>
    </div>

    <div class="card stat-card">
        <div class="stat-icon">📅</div>
        <div class="stat-details">
            <h3><c:out value="${sessions.size()}" /></h3>
            <p>Study Sessions</p>
            <a href="<c:url value='/sessions' />" style="font-size: 0.8rem; color: var(--primary);">Manage Sessions →</a>
        </div>
    </div>

    <div class="card stat-card">
        <div class="stat-icon">📑</div>
        <div class="stat-details">
            <h3><c:out value="${notesCount}" /></h3>
            <p>Shared Notes</p>
            <a href="<c:url value='/notes' />" style="font-size: 0.8rem; color: var(--primary);">Browse Notes →</a>
        </div>
    </div>

    <div class="card stat-card">
        <div class="stat-icon">⭐</div>
        <div class="stat-details">
            <h3><c:out value="${averageRating}" /> / 5.0</h3>
            <p>Peer Rating</p>
            <a href="<c:url value='/ratings' />" style="font-size: 0.8rem; color: var(--primary);">View Reviews →</a>
        </div>
    </div>
</div>

<!-- Current Skills Snapshot -->
<div class="card" style="margin-bottom: 2rem;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
        <h3 style="font-size: 1.15rem; font-weight: 700; color: var(--secondary);">My Skills Profile</h3>
        <a href="<c:url value='/profile' />" class="btn btn-secondary btn-sm">Edit Skills & Profile</a>
    </div>

    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem;">
        <div>
            <h4 style="font-size: 0.9rem; color: var(--text-muted); margin-bottom: 0.5rem; text-transform: uppercase;">
                Skills I Know (Can Tutor):
            </h4>
            <div>
                <c:choose>
                    <c:when test="${not empty student.skillsKnown}">
                        <c:forEach var="skill" items="${student.skillsKnown}">
                            <span class="badge badge-skill"><c:out value="${skill}" /></span>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <p style="font-size: 0.85rem; color: var(--text-muted);">No known skills listed yet. Add some in your profile!</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div>
            <h4 style="font-size: 0.9rem; color: var(--text-muted); margin-bottom: 0.5rem; text-transform: uppercase;">
                Skills I Need Help With:
            </h4>
            <div>
                <c:choose>
                    <c:when test="${not empty student.skillsNeeded}">
                        <c:forEach var="skill" items="${student.skillsNeeded}">
                            <span class="badge badge-skill badge-needed"><c:out value="${skill}" /></span>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <p style="font-size: 0.85rem; color: var(--text-muted);">No needed skills listed yet. Add what you want to study!</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<!-- Scheduled Sessions Table -->
<div class="card">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
        <h3 style="font-size: 1.15rem; font-weight: 700; color: var(--secondary);">Recent Study Sessions</h3>
        <a href="<c:url value='/sessions' />" style="font-size: 0.85rem; color: var(--primary); font-weight: 600;">View All Sessions →</a>
    </div>

    <c:choose>
        <c:when test="${not empty sessions}">
            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Subject</th>
                            <th>Date</th>
                            <th>Time</th>
                            <th>Participants</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="s" items="${sessions}" begin="0" end="4">
                            <tr>
                                <td>#<c:out value="${s.sessionId}" /></td>
                                <td><strong><c:out value="${s.subject}" /></strong></td>
                                <td><c:out value="${s.dateDisplay}" /></td>
                                <td><c:out value="${s.timeDisplay}" /></td>
                                <td>
                                    <c:forEach var="p" items="${s.participants}" varStatus="status">
                                        <c:out value="${p.name}" /><c:if test="${!status.last}">, </c:if>
                                    </c:forEach>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${s.status == 'SCHEDULED'}"><span class="badge badge-scheduled">SCHEDULED</span></c:when>
                                        <c:when test="${s.status == 'COMPLETED'}"><span class="badge badge-completed">COMPLETED</span></c:when>
                                        <c:when test="${s.status == 'CANCELLED'}"><span class="badge badge-cancelled">CANCELLED</span></c:when>
                                    </c:choose>
                                </td>
                                <td>
                                    <a href="<c:url value='/sessions?id=${s.sessionId}' />" class="btn btn-secondary btn-sm">Details</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:when>
        <c:otherwise>
            <div class="empty-state">
                <div class="empty-state-icon">📅</div>
                <p>No study sessions scheduled yet. Find a study buddy and send a request!</p>
                <a href="<c:url value='/buddies' />" class="btn btn-primary btn-sm" style="margin-top: 1rem;">Find Study Buddies</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
