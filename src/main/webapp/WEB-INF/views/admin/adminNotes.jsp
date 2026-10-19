<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Moderate Notes - Admin Console" scope="request" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-header">
    <div>
        <a href="<c:url value='/admin/dashboard' />" style="font-size: 0.85rem; color: var(--primary); text-decoration: none;">&larr; Back to Admin Dashboard</a>
        <h1 class="page-title" style="margin-top: 0.5rem;">Academic Notes Moderation</h1>
        <p class="page-subtitle">Review all student-uploaded documents; purge inappropriate or copyright-infringing materials.</p>
    </div>
</div>

<div class="card">
    <c:choose>
        <c:when test="${not empty notes}">
            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Note ID</th>
                            <th>Title</th>
                            <th>Subject</th>
                            <th>Uploader</th>
                            <th>Filename</th>
                            <th>Size</th>
                            <th>Uploaded At</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="n" items="${notes}">
                            <tr>
                                <td>#<c:out value="${n.noteId}" /></td>
                                <td><strong><c:out value="${n.title}" /></strong></td>
                                <td><span class="badge badge-skill"><c:out value="${n.subject}" /></span></td>
                                <td><c:out value="${n.uploaderName}" /></td>
                                <td><c:out value="${n.originalFilename}" /></td>
                                <td><c:out value="${n.fileSizeDisplay}" /></td>
                                <td><c:out value="${n.uploadedAtDisplay}" /></td>
                                <td>
                                    <div style="display: flex; gap: 0.5rem; align-items: center;">
                                        <a href="<c:url value='/notes/download?id=${n.noteId}' />" class="btn btn-secondary btn-sm">
                                            ⬇ Audit File
                                        </a>

                                        <form action="<c:url value='/admin/notes' />" method="post" style="margin: 0;">
                                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                                            <input type="hidden" name="action" value="delete" />
                                            <input type="hidden" name="noteId" value="<c:out value='${n.noteId}' />" />
                                            <button type="submit" class="btn btn-danger btn-sm"
                                                    data-confirm="Delete note #${n.noteId} ('${n.title}')? This deletes both database metadata and the stored file.">
                                                Delete
                                            </button>
                                        </form>
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
                <p>No notes currently stored in the system.</p>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
