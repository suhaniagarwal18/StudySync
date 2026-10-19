<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Study Requests - StudySync" scope="request" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-header">
    <div>
        <h1 class="page-title">Peer Study Requests</h1>
        <p class="page-subtitle">Track incoming study invitations and check status of requests you sent.</p>
    </div>
    <a href="<c:url value='/buddies' />" class="btn btn-primary">+ Find New Buddies</a>
</div>

<!-- Received Requests Section -->
<div class="card" style="margin-bottom: 2rem;">
    <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 1rem; color: var(--secondary);">
        📥 Incoming Requests (Sent to You)
    </h2>

    <c:choose>
        <c:when test="${not empty receivedRequests}">
            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Sender</th>
                            <th>Department</th>
                            <th>Requested Subject</th>
                            <th>Sent Date</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="reqItem" items="${receivedRequests}">
                            <tr>
                                <td><strong><c:out value="${reqItem.sender.name}" /></strong></td>
                                <td><c:out value="${reqItem.sender.department}" /></td>
                                <td><span class="badge badge-skill"><c:out value="${reqItem.subject}" /></span></td>
                                <td><c:out value="${reqItem.createdAtDisplay}" /></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${reqItem.status == 'PENDING'}"><span class="badge badge-pending">PENDING</span></c:when>
                                        <c:when test="${reqItem.status == 'ACCEPTED'}"><span class="badge badge-accepted">ACCEPTED</span></c:when>
                                        <c:when test="${reqItem.status == 'REJECTED'}"><span class="badge badge-rejected">REJECTED</span></c:when>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:if test="${reqItem.status == 'PENDING'}">
                                        <div style="display: flex; gap: 0.5rem;">
                                            <form action="<c:url value='/requests' />" method="post" style="margin: 0;">
                                                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                                                <input type="hidden" name="action" value="accept" />
                                                <input type="hidden" name="requestId" value="<c:out value='${reqItem.requestId}' />" />
                                                <button type="submit" class="btn btn-success btn-sm">Accept</button>
                                            </form>
                                            <form action="<c:url value='/requests' />" method="post" style="margin: 0;">
                                                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                                                <input type="hidden" name="action" value="reject" />
                                                <input type="hidden" name="requestId" value="<c:out value='${reqItem.requestId}' />" />
                                                <button type="submit" class="btn btn-danger btn-sm" data-confirm="Decline this study request?">Decline</button>
                                            </form>
                                        </div>
                                    </c:if>
                                    <c:if test="${reqItem.status == 'ACCEPTED'}">
                                        <a href="<c:url value='/sessions?scheduleRequestId=${reqItem.requestId}' />" class="btn btn-primary btn-sm">
                                            📅 Schedule Session
                                        </a>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:when>
        <c:otherwise>
            <div class="empty-state">
                <p>No incoming study requests right now.</p>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<!-- Sent Requests Section -->
<div class="card">
    <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 1rem; color: var(--secondary);">
        📤 Outgoing Requests (Sent by You)
    </h2>

    <c:choose>
        <c:when test="${not empty sentRequests}">
            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Peer Student</th>
                            <th>Department</th>
                            <th>Subject</th>
                            <th>Sent Date</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="reqItem" items="${sentRequests}">
                            <tr>
                                <td><strong><c:out value="${reqItem.receiver.name}" /></strong></td>
                                <td><c:out value="${reqItem.receiver.department}" /></td>
                                <td><span class="badge badge-skill"><c:out value="${reqItem.subject}" /></span></td>
                                <td><c:out value="${reqItem.createdAtDisplay}" /></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${reqItem.status == 'PENDING'}"><span class="badge badge-pending">PENDING</span></c:when>
                                        <c:when test="${reqItem.status == 'ACCEPTED'}"><span class="badge badge-accepted">ACCEPTED</span></c:when>
                                        <c:when test="${reqItem.status == 'REJECTED'}"><span class="badge badge-rejected">REJECTED</span></c:when>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:if test="${reqItem.status == 'ACCEPTED'}">
                                        <a href="<c:url value='/sessions?scheduleRequestId=${reqItem.requestId}' />" class="btn btn-primary btn-sm">
                                            📅 Schedule Session
                                        </a>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:when>
        <c:otherwise>
            <div class="empty-state">
                <p>You have not sent any study requests yet.</p>
                <a href="<c:url value='/buddies' />" class="btn btn-primary btn-sm" style="margin-top: 0.5rem;">Find Study Buddies</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
