<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Find Study Buddies - StudySync" scope="request" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-header">
    <div>
        <h1 class="page-title">Find Study Buddies</h1>
        <p class="page-subtitle">Connect with peers whose skills match your learning goals.</p>
    </div>
</div>

<!-- Search by Subject Filter -->
<div class="card" style="margin-bottom: 2rem;">
    <form action="<c:url value='/buddies' />" method="get" style="display: flex; gap: 1rem; align-items: center; flex-wrap: wrap;">
        <div style="flex: 1; min-width: 250px;">
            <input type="text" name="subject" class="form-control"
                   list="skillsList" placeholder="Search students by specific subject (e.g. Java, DBMS, DSA, Python)..."
                   value="<c:out value='${searchSubject}' />" />
            <datalist id="skillsList">
                <c:forEach var="s" items="${allSkills}">
                    <option value="<c:out value='${s}' />" />
                </c:forEach>
            </datalist>
        </div>
        <button type="submit" class="btn btn-primary">Search</button>
        <c:if test="${isSubjectSearch}">
            <a href="<c:url value='/buddies' />" class="btn btn-secondary">Reset to Smart Recommendations</a>
        </c:if>
    </form>
</div>

<!-- Matches Results Container -->
<c:choose>
    <c:when test="${not empty matches}">
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(350px, 1fr)); gap: 1.5rem;">
            <c:forEach var="m" items="${matches}">
                <div class="card" style="display: flex; flex-direction: column; justify-content: space-between; position: relative;">
                    <c:if test="${m.mutualMatch}">
                        <div style="position: absolute; top: 1rem; right: 1rem;">
                            <span class="badge badge-mutual">★ MUTUAL MATCH</span>
                        </div>
                    </c:if>

                    <div>
                        <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.75rem;">
                            <div style="width: 48px; height: 48px; border-radius: 50%; background: var(--primary-light); color: var(--primary-dark); display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 1.25rem;">
                                <c:out value="${fn:substring(m.student.name, 0, 1)}" />
                            </div>
                            <div>
                                <h3 style="font-size: 1.15rem; font-weight: 700; color: var(--secondary); margin-bottom: 0.15rem;">
                                    <c:out value="${m.student.name}" />
                                </h3>
                                <p style="font-size: 0.85rem; color: var(--text-muted);"><c:out value="${m.student.department}" /></p>
                            </div>
                        </div>

                        <c:if test="${not empty m.student.bio}">
                            <p style="font-size: 0.85rem; color: var(--text-main); margin-bottom: 1rem; font-style: italic;">
                                "<c:out value="${m.student.bio}" />"
                            </p>
                        </c:if>

                        <div style="display: flex; gap: 1rem; margin-bottom: 1rem; font-size: 0.85rem;">
                            <div>
                                <span style="color: var(--text-muted);">Match Score:</span>
                                <strong style="color: var(--primary); font-size: 1rem;"><c:out value="${m.matchScore}" /> pts</strong>
                            </div>
                            <div>
                                <span style="color: var(--text-muted);">Rating:</span>
                                <strong style="color: var(--accent); font-size: 1rem;">★ <c:out value="${m.formattedRating}" /></strong>
                            </div>
                        </div>

                        <div style="margin-bottom: 1rem;">
                            <h4 style="font-size: 0.8rem; text-transform: uppercase; color: var(--text-muted); margin-bottom: 0.35rem;">
                                Matched Subject Skills:
                            </h4>
                            <div>
                                <c:forEach var="cSkill" items="${m.commonSkills}">
                                    <span class="badge badge-skill"><c:out value="${cSkill}" /></span>
                                </c:forEach>
                            </div>
                        </div>
                    </div>

                    <!-- Request Form -->
                    <div style="border-top: 1px solid var(--border); padding-top: 1rem; margin-top: 1rem;">
                        <form action="<c:url value='/requests' />" method="post" style="display: flex; gap: 0.5rem;">
                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                            <input type="hidden" name="action" value="send" />
                            <input type="hidden" name="receiverId" value="<c:out value='${m.student.userId}' />" />

                            <select name="subject" class="form-control" style="font-size: 0.85rem; padding: 0.4rem 0.6rem;" required>
                                <c:forEach var="cSkill" items="${m.commonSkills}">
                                    <option value="<c:out value='${cSkill}' />"><c:out value="${cSkill}" /></option>
                                </c:forEach>
                            </select>

                            <button type="submit" class="btn btn-primary btn-sm" style="white-space: nowrap;">
                                Send Request
                            </button>
                        </form>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:when>
    <c:otherwise>
        <div class="card empty-state">
            <div class="empty-state-icon">🔍</div>
            <h3 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 0.5rem; color: var(--secondary);">
                No Matching Study Buddies Found
            </h3>
            <p style="max-width: 500px; margin: 0 auto 1.5rem auto;">
                <c:choose>
                    <c:when test="${isSubjectSearch}">
                        No active students currently have listed knowledge in "<strong><c:out value='${searchSubject}' /></strong>". Try another keyword or browse smart recommendations.
                    </c:when>
                    <c:otherwise>
                        We couldn't find matching students right now. Make sure you have added subjects you want to learn under <strong>Skills I Need</strong> in your profile.
                    </c:otherwise>
                </c:choose>
            </p>
            <a href="<c:url value='/profile' />" class="btn btn-primary">Update My Needed Skills</a>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
