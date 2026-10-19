<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="User Management - Admin Console" scope="request" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-header">
    <div>
        <a href="<c:url value='/admin/dashboard' />" style="font-size: 0.85rem; color: var(--primary); text-decoration: none;">&larr; Back to Admin Dashboard</a>
        <h1 class="page-title" style="margin-top: 0.5rem;">User Account Management</h1>
        <p class="page-subtitle">Inspect registered students and administrators; toggle account activation statuses.</p>
    </div>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>User ID</th>
                    <th>Full Name</th>
                    <th>Email Address</th>
                    <th>Department</th>
                    <th>Role</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="u" items="${users}">
                    <tr>
                        <td>#<c:out value="${u.userId}" /></td>
                        <td><strong><c:out value="${u.name}" /></strong></td>
                        <td><c:out value="${u.email}" /></td>
                        <td><c:out value="${u.department}" /></td>
                        <td>
                            <span class="badge ${u.role == 'ADMIN' ? 'badge-mutual' : 'badge-skill'}">
                                <c:out value="${u.role}" />
                            </span>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${u.active}">
                                    <span class="badge badge-active">ACTIVE</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge badge-inactive">INACTIVE</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${u.userId == sessionScope.user.userId}">
                                    <span style="font-size: 0.8rem; color: var(--text-muted); font-style: italic;">
                                        (You - Self Protected)
                                    </span>
                                </c:when>
                                <c:otherwise>
                                    <form action="<c:url value='/admin/users' />" method="post" style="margin: 0;">
                                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                                        <input type="hidden" name="action" value="toggleStatus" />
                                        <input type="hidden" name="targetUserId" value="<c:out value='${u.userId}' />" />
                                        <c:choose>
                                            <c:when test="${u.active}">
                                                <button type="submit" class="btn btn-danger btn-sm"
                                                        data-confirm="Deactivate account for ${u.name}? They will no longer be able to log in.">
                                                    Deactivate
                                                </button>
                                            </c:when>
                                            <c:otherwise>
                                                <button type="submit" class="btn btn-success btn-sm"
                                                        data-confirm="Reactivate account for ${u.name}?">
                                                    Activate
                                                </button>
                                            </c:otherwise>
                                        </c:choose>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
