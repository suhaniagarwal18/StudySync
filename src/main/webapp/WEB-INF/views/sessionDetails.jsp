<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Session Details - StudySync" scope="request" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-header">
    <div>
        <a href="<c:url value='/sessions' />" style="font-size: 0.85rem; color: var(--primary); text-decoration: none;">&larr; Back to All Sessions</a>
        <h1 class="page-title" style="margin-top: 0.5rem;">
            Study Session #<c:out value="${studySession.sessionId}" />: <c:out value="${studySession.subject}" />
        </h1>
        <p class="page-subtitle">
            Scheduled for <c:out value="${studySession.dateDisplay}" /> at <c:out value="${studySession.timeDisplay}" />
        </p>
    </div>
    <div>
        <c:choose>
            <c:when test="${studySession.status == 'SCHEDULED'}"><span class="badge badge-scheduled" style="font-size: 0.9rem; padding: 0.4rem 0.8rem;">SCHEDULED</span></c:when>
            <c:when test="${studySession.status == 'COMPLETED'}"><span class="badge badge-completed" style="font-size: 0.9rem; padding: 0.4rem 0.8rem;">COMPLETED</span></c:when>
            <c:when test="${studySession.status == 'CANCELLED'}"><span class="badge badge-cancelled" style="font-size: 0.9rem; padding: 0.4rem 0.8rem;">CANCELLED</span></c:when>
        </c:choose>
    </div>
</div>

<div style="display: grid; grid-template-columns: 2fr 1fr; gap: 2rem;">
    <!-- Left Column: Details & Actions -->
    <div>
        <!-- Participants Card -->
        <div class="card" style="margin-bottom: 1.5rem;">
            <h2 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 1rem; color: var(--secondary);">
                Session Participants
            </h2>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                <c:forEach var="p" items="${studySession.participants}">
                    <div style="display: flex; gap: 0.75rem; align-items: center; padding: 0.75rem; background: #f8fafc; border-radius: var(--radius); border: 1px solid var(--border);">
                        <div style="width: 42px; height: 42px; border-radius: 50%; background: var(--primary-light); color: var(--primary-dark); display: flex; align-items: center; justify-content: center; font-weight: 700;">
                            <c:out value="${fn:substring(p.name, 0, 1)}" />
                        </div>
                        <div>
                            <strong><c:out value="${p.name}" /></strong>
                            <p style="font-size: 0.8rem; color: var(--text-muted);"><c:out value="${p.department}" /></p>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>

        <!-- Session Status Controls -->
        <div class="card" style="margin-bottom: 1.5rem;">
            <h2 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 1rem; color: var(--secondary);">
                Session Controls & Lifecycle
            </h2>

            <c:choose>
                <c:when test="${studySession.status == 'SCHEDULED'}">
                    <p style="font-size: 0.9rem; color: var(--text-muted); margin-bottom: 1rem;">
                        This session is currently active. Once you have concluded your peer discussion, mark it as completed so you and your study buddy can exchange ratings.
                    </p>
                    <div style="display: flex; gap: 1rem;">
                        <form action="<c:url value='/sessions' />" method="post">
                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                            <input type="hidden" name="action" value="complete" />
                            <input type="hidden" name="sessionId" value="<c:out value='${studySession.sessionId}' />" />
                            <button type="submit" class="btn btn-success">✓ Mark Session as Completed</button>
                        </form>

                        <form action="<c:url value='/sessions' />" method="post">
                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                            <input type="hidden" name="action" value="cancel" />
                            <input type="hidden" name="sessionId" value="<c:out value='${studySession.sessionId}' />" />
                            <button type="submit" class="btn btn-danger" data-confirm="Are you sure you want to cancel this study session?">Cancel Session</button>
                        </form>
                    </div>
                </c:when>
                <c:when test="${studySession.status == 'COMPLETED'}">
                    <p style="font-size: 0.9rem; color: #065f46; background: var(--success-light); padding: 0.75rem; border-radius: var(--radius); border-left: 4px solid var(--success);">
                        ✓ This session was marked as completed. Peer evaluations are open.
                    </p>
                </c:when>
                <c:when test="${studySession.status == 'CANCELLED'}">
                    <p style="font-size: 0.9rem; color: #991b1b; background: var(--danger-light); padding: 0.75rem; border-radius: var(--radius); border-left: 4px solid var(--danger);">
                        This session has been cancelled.
                    </p>
                </c:when>
            </c:choose>
        </div>

        <!-- Rating Submission Form (For Completed Sessions) -->
        <c:if test="${studySession.status == 'COMPLETED'}">
            <div class="card">
                <h2 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 1rem; color: var(--secondary);">
                    Peer Review & Rating
                </h2>

                <c:choose>
                    <c:when test="${hasRated}">
                        <p style="font-size: 0.9rem; color: var(--text-muted); background: #f8fafc; padding: 0.75rem; border-radius: var(--radius);">
                            ✓ You have already submitted feedback for this study session. Thank you!
                        </p>
                    </c:when>
                    <c:otherwise>
                        <form action="<c:url value='/ratings' />" method="post">
                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                            <input type="hidden" name="sessionId" value="<c:out value='${studySession.sessionId}' />" />

                            <div class="form-group">
                                <label class="form-label">Overall Experience Rating (1 to 5 Stars)</label>
                                <select name="ratingValue" class="form-control" required style="max-width: 250px;">
                                    <option value="5" selected>★★★★★ (5 - Excellent Tutoring)</option>
                                    <option value="4">★★★★☆ (4 - Very Helpful)</option>
                                    <option value="3">★★★☆☆ (3 - Good Discussion)</option>
                                    <option value="2">★★☆☆☆ (2 - Fair)</option>
                                    <option value="1">★☆☆☆☆ (1 - Needs Improvement)</option>
                                </select>
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="feedback">Constructive Feedback (Optional)</label>
                                <textarea id="feedback" name="feedback" rows="3" class="form-control"
                                          placeholder="Share what went well or which topics were explained clearly..."></textarea>
                            </div>

                            <button type="submit" class="btn btn-primary">Submit Peer Review</button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>
    </div>

    <!-- Right Column: Binary Serialization Demonstration -->
    <div>
        <div class="card" style="border-top: 4px solid var(--accent);">
            <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.75rem;">
                <span style="font-size: 1.25rem;">📦</span>
                <h3 style="font-size: 1.1rem; font-weight: 700; color: var(--secondary);">
                    Serialization Demo
                </h3>
            </div>
            <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1rem;">
                CONCEPT: Demonstrates <code>ObjectOutputStream</code> &amp; <code>ObjectInputStream</code> snapshot serialization with <code>transient</code> caching.
            </p>

            <form action="<c:url value='/sessions' />" method="post" style="margin-bottom: 1rem;">
                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                <input type="hidden" name="action" value="exportSummary" />
                <input type="hidden" name="sessionId" value="<c:out value='${studySession.sessionId}' />" />
                <button type="submit" class="btn btn-secondary btn-sm" style="width: 100%;">
                    💾 Export Serialized Summary
                </button>
            </form>

            <c:choose>
                <c:when test="${not empty sessionSummary}">
                    <div style="background: #f8fafc; border: 1px solid #cbd5e1; border-radius: var(--radius); padding: 0.85rem; font-size: 0.8rem;">
                        <p style="font-weight: 700; color: var(--secondary); margin-bottom: 0.35rem;">Deserialized Data Loaded:</p>
                        <p><strong>Session:</strong> #<c:out value="${sessionSummary.sessionId}" /> (<c:out value="${sessionSummary.subject}" />)</p>
                        <p><strong>Export Date:</strong> <c:out value="${sessionSummary.exportedAtDisplay}" /></p>
                        <p><strong>Status:</strong> <c:out value="${sessionSummary.status}" /></p>
                        <p style="margin-top: 0.5rem; font-family: monospace; font-size: 0.75rem; background: #fff; padding: 0.5rem; border-radius: 4px; border: 1px solid var(--border);">
                            <c:out value="${sessionSummary.summaryDisplay}" />
                        </p>
                    </div>
                </c:when>
                <c:otherwise>
                    <p style="font-size: 0.8rem; color: var(--text-muted); font-style: italic;">
                        No serialized snapshot created yet. Click "Export Serialized Summary" to generate one.
                    </p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
