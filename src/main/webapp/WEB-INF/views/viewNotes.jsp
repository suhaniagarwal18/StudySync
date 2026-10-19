<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Notes Exchange - StudySync" scope="request" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-header">
    <div>
        <h1 class="page-title">Lecture Notes Exchange</h1>
        <p class="page-subtitle">Browse and download peer-reviewed university lecture notes and study guides.</p>
    </div>
    <a href="<c:url value='/notes/upload' />" class="btn btn-primary">+ Upload New Note</a>
</div>

<!-- Search & Subject Filter Bar -->
<div class="card" style="margin-bottom: 2rem;">
    <form action="<c:url value='/notes' />" method="get" style="display: flex; gap: 1rem; flex-wrap: wrap; align-items: center;">
        <div style="flex: 2; min-width: 250px;">
            <input type="text" name="keyword" class="form-control"
                   placeholder="Search by note title, subject, or module..."
                   value="<c:out value='${keyword}' />" />
        </div>

        <div style="flex: 1; min-width: 180px;">
            <select name="subject" class="form-control">
                <option value="">-- All Subjects --</option>
                <c:forEach var="sub" items="${distinctSubjects}">
                    <option value="<c:out value='${sub}' />" ${selectedSubject == sub ? 'selected' : ''}>
                        <c:out value="${sub}" />
                    </option>
                </c:forEach>
            </select>
        </div>

        <button type="submit" class="btn btn-primary">Filter Notes</button>

        <c:if test="${not empty keyword or not empty selectedSubject}">
            <a href="<c:url value='/notes' />" class="btn btn-secondary">Clear</a>
        </c:if>
    </form>
</div>

<!-- Notes Catalog Table -->
<div class="card">
    <c:choose>
        <c:when test="${not empty notes}">
            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Document Title</th>
                            <th>Subject</th>
                            <th>Module</th>
                            <th>Uploader</th>
                            <th>Size</th>
                            <th>Uploaded On</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="n" items="${notes}">
                            <tr>
                                <td>
                                    <strong><c:out value="${n.title}" /></strong>
                                    <div style="font-size: 0.75rem; color: var(--text-muted);">
                                        <c:out value="${n.originalFilename}" />
                                    </div>
                                </td>
                                <td><span class="badge badge-skill"><c:out value="${n.subject}" /></span></td>
                                <td><c:out value="${not empty n.module ? n.module : '-'}" /></td>
                                <td><c:out value="${n.uploaderName}" /></td>
                                <td><c:out value="${n.fileSizeDisplay}" /></td>
                                <td><c:out value="${n.uploadedAtDisplay}" /></td>
                                <td>
                                    <div style="display: flex; gap: 0.5rem; align-items: center;">
                                        <a href="<c:url value='/notes/download?id=${n.noteId}' />" class="btn btn-primary btn-sm">
                                            ⬇ Download
                                        </a>

                                        <c:if test="${sessionScope.user.userId == n.userId or sessionScope.user.role == 'ADMIN'}">
                                            <form action="<c:url value='/notes' />" method="post" style="margin: 0;">
                                                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                                                <input type="hidden" name="action" value="delete" />
                                                <input type="hidden" name="noteId" value="<c:out value='${n.noteId}' />" />
                                                <button type="submit" class="btn btn-danger btn-sm" data-confirm="Are you sure you want to delete this note?">
                                                    Delete
                                                </button>
                                            </form>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:when>
        <c:otherwise>
            <div class="empty-state">
                <div class="empty-state-icon">📑</div>
                <h3 style="font-size: 1.15rem; font-weight: 600; margin-bottom: 0.5rem;">No Notes Found</h3>
                <p>No lecture notes match your filter criteria. Be the first to share your notes!</p>
                <a href="<c:url value='/notes/upload' />" class="btn btn-primary btn-sm" style="margin-top: 1rem;">Upload Notes</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
