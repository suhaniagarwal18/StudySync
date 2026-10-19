<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="My Ratings & Reviews - StudySync" scope="request" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-header">
    <div>
        <h1 class="page-title">My Peer Feedback &amp; Ratings</h1>
        <p class="page-subtitle">Evaluations and constructive feedback received from study buddies after completed sessions.</p>
    </div>
</div>

<!-- Rating Score Summary Card -->
<div class="card stat-card" style="margin-bottom: 2rem; max-width: 400px;">
    <div class="stat-icon" style="background: #fef3c7; color: #b45309; font-size: 2rem;">⭐</div>
    <div class="stat-details">
        <h3 style="color: #b45309;"><c:out value="${averageRating}" /> / 5.0</h3>
        <p>Overall Student Reputation</p>
    </div>
</div>

<!-- Reviews Received List -->
<div class="card">
    <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 1.25rem; color: var(--secondary);">
        Received Peer Reviews (<c:out value="${ratings.size()}" />)
    </h2>

    <c:choose>
        <c:when test="${not empty ratings}">
            <div style="display: flex; flex-direction: column; gap: 1rem;">
                <c:forEach var="r" items="${ratings}">
                    <div style="padding: 1.25rem; background: #f8fafc; border-radius: var(--radius); border: 1px solid var(--border);">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; flex-wrap: wrap;">
                            <div>
                                <strong style="font-size: 1rem; color: var(--secondary);"><c:out value="${r.reviewerName}" /></strong>
                                <span style="font-size: 0.85rem; color: var(--text-muted); margin-left: 0.5rem;">
                                    on subject <strong><c:out value="${r.sessionSubject}" /></strong>
                                </span>
                            </div>
                            <span style="font-size: 0.85rem; color: var(--text-muted);"><c:out value="${r.createdAtDisplay}" /></span>
                        </div>

                        <div style="color: var(--accent); font-size: 1.1rem; margin-bottom: 0.5rem;">
                            <c:out value="${r.starsDisplay}" />
                        </div>

                        <c:if test="${not empty r.feedback}">
                            <p style="font-size: 0.95rem; color: var(--text-main); font-style: italic;">
                                "<c:out value="${r.feedback}" />"
                            </p>
                        </c:if>
                    </div>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <div class="empty-state">
                <div class="empty-state-icon">⭐</div>
                <h3 style="font-size: 1.15rem; font-weight: 600; margin-bottom: 0.5rem;">No Ratings Received Yet</h3>
                <p>Complete your scheduled study sessions with your peers to earn reputation and feedback!</p>
                <a href="<c:url value='/sessions' />" class="btn btn-primary btn-sm" style="margin-top: 1rem;">View Study Sessions</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
