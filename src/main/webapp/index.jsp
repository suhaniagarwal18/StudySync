<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="StudySync - Peer Study Buddy & Notes Exchange" scope="request" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div style="text-align: center; padding: 3rem 1rem;">
    <h1 style="font-size: 2.75rem; font-weight: 800; color: var(--secondary); margin-bottom: 1rem;">
        Find Your Perfect <span style="color: var(--primary);">Study Buddy</span> & Exchange Notes
    </h1>
    <p style="font-size: 1.2rem; color: var(--text-muted); max-width: 700px; margin: 0 auto 2rem auto;">
        StudySync connects university students based on complementary academic skills. Learn what you need, share what you know, schedule peer sessions, and access verified lecture resources.
    </p>

    <div style="display: flex; gap: 1rem; justify-content: center; margin-bottom: 3rem;">
        <c:choose>
            <c:when test="${not empty sessionScope.user}">
                <a href="<c:url value='${sessionScope.user.dashboardPath}' />" class="btn btn-primary" style="font-size: 1.1rem; padding: 0.85rem 1.75rem;">
                    Go to Your Dashboard →
                </a>
            </c:when>
            <c:otherwise>
                <a href="<c:url value='/register' />" class="btn btn-primary" style="font-size: 1.1rem; padding: 0.85rem 1.75rem;">
                    Get Started Free
                </a>
                <a href="<c:url value='/login' />" class="btn btn-secondary" style="font-size: 1.1rem; padding: 0.85rem 1.75rem;">
                    Student Login
                </a>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Core Features Grid -->
    <div class="card-grid" style="text-align: left; margin-top: 2rem;">
        <div class="card">
            <div style="font-size: 2rem; margin-bottom: 0.75rem;">🤝</div>
            <h3 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 0.5rem;">Smart Mutual Matching</h3>
            <p style="color: var(--text-muted); font-size: 0.95rem;">
                Our set-intersection algorithm matches you with peers where your known skills satisfy their learning needs and vice versa.
            </p>
        </div>
        <div class="card">
            <div style="font-size: 2rem; margin-bottom: 0.75rem;">📅</div>
            <h3 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 0.5rem;">Structured Study Sessions</h3>
            <p style="color: var(--text-muted); font-size: 0.95rem;">
                Send study requests, confirm dates and times, and track session status from scheduled to completion with built-in audit trails.
            </p>
        </div>
        <div class="card">
            <div style="font-size: 2rem; margin-bottom: 0.75rem;">📑</div>
            <h3 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 0.5rem;">Verified Notes Exchange</h3>
            <p style="color: var(--text-muted); font-size: 0.95rem;">
                Upload, filter, and download curriculum lecture notes with strict file verification and antivirus-grade magic-byte security checks.
            </p>
        </div>
        <div class="card">
            <div style="font-size: 2rem; margin-bottom: 0.75rem;">⭐</div>
            <h3 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 0.5rem;">Peer Ratings & Trust</h3>
            <p style="color: var(--text-muted); font-size: 0.95rem;">
                Rate peer tutors and receive constructive feedback after each completed session to build academic reputation and trust.
            </p>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
