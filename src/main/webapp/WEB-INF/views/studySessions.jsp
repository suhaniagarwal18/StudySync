<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Study Sessions - StudySync" scope="request" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-header">
    <div>
        <h1 class="page-title">Peer Study Sessions</h1>
        <p class="page-subtitle">Collaborate, track scheduled meeting times, and record outcomes.</p>
    </div>
</div>

<!-- Scheduling Box (Shown when triggered from an accepted study request) -->
<c:if test="${not empty scheduleRequest}">
    <div class="card" style="margin-bottom: 2rem; border-left: 4px solid var(--primary);">
        <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 0.5rem; color: var(--secondary);">
            📅 Schedule Study Session for Accepted Request #<c:out value="${scheduleRequest.requestId}" />
        </h2>
        <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1.25rem;">
            Subject: <strong><c:out value="${scheduleRequest.subject}" /></strong> | 
            Participants: <strong><c:out value="${scheduleRequest.sender.name}" /></strong> & <strong><c:out value="${scheduleRequest.receiver.name}" /></strong>
        </p>

        <form action="<c:url value='/sessions' />" method="post" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)) auto; gap: 1rem; align-items: flex-end;">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
            <input type="hidden" name="action" value="schedule" />
            <input type="hidden" name="requestId" value="<c:out value='${scheduleRequest.requestId}' />" />

            <div class="form-group" style="margin-bottom: 0;">
                <label class="form-label" for="sessionDate">Session Date</label>
                <input type="date" id="sessionDate" name="sessionDate" class="form-control" required />
            </div>

            <div class="form-group" style="margin-bottom: 0;">
                <label class="form-label" for="sessionTime">Session Time</label>
                <input type="time" id="sessionTime" name="sessionTime" class="form-control" required />
            </div>

            <button type="submit" class="btn btn-primary" style="height: 42px;">Confirm & Schedule</button>
        </form>
    </div>
</c:if>

<!-- All Study Sessions Table -->
<div class="card">
    <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 1rem; color: var(--secondary);">
        My Scheduled & Historical Sessions
    </h2>

    <c:choose>
        <c:when test="${not empty sessions}">
            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Session ID</th>
                            <th>Subject</th>
                            <th>Date</th>
                            <th>Time</th>
                            <th>Peer Participants</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="s" items="${sessions}">
                            <tr>
                                <td>#<c:out value="${s.sessionId}" /></td>
                                <td><strong><c:out value="${s.subject}" /></strong></td>
                                <td><c:out value="${s.dateDisplay}" /></td>
                                <td><c:out value="${s.timeDisplay}" /></td>
                                <td>
                                    <c:forEach var="p" items="${s.participants}" varStatus="status">
                                        <c:out value="${p.name}" /> (<c:out value="${p.department}" />)<c:if test="${!status.last}">, </c:if>
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
                                    <a href="<c:url value='/sessions?id=${s.sessionId}' />" class="btn btn-secondary btn-sm">
                                        View Details →
                                    </a>
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
                <h3 style="font-size: 1.15rem; font-weight: 600; margin-bottom: 0.5rem;">No Study Sessions Yet</h3>
                <p>Accept an incoming study request or send one to get your first session scheduled.</p>
                <div style="margin-top: 1rem; display: flex; gap: 0.5rem; justify-content: center;">
                    <a href="<c:url value='/requests' />" class="btn btn-secondary btn-sm">View Requests</a>
                    <a href="<c:url value='/buddies' />" class="btn btn-primary btn-sm">Find Buddies</a>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
