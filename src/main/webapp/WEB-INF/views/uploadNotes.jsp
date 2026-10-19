<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Upload Lecture Notes - StudySync" scope="request" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-header">
    <div>
        <h1 class="page-title">Upload Study Notes</h1>
        <p class="page-subtitle">Share high-quality lecture materials, cheatsheets, and summaries with your peers.</p>
    </div>
    <a href="<c:url value='/notes' />" class="btn btn-secondary">&larr; Back to Notes Exchange</a>
</div>

<div class="form-container" style="max-width: 600px;">
    <form action="<c:url value='/notes/upload' />" method="post" enctype="multipart/form-data">
        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />

        <div class="form-group">
            <label class="form-label" for="title">Document Title</label>
            <input type="text" id="title" name="title" class="form-control"
                   value="<c:out value='${title}' />" required placeholder="e.g. Java 17 OOP & Collections Full Notes" />
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
            <div class="form-group">
                <label class="form-label" for="subject">Curriculum Subject</label>
                <input type="text" id="subject" name="subject" class="form-control"
                       list="skillsList" value="<c:out value='${subject}' />" required placeholder="e.g. Java, DBMS, DSA" />
                <datalist id="skillsList">
                    <c:forEach var="s" items="${allSkills}">
                        <option value="<c:out value='${s}' />" />
                    </c:forEach>
                </datalist>
            </div>

            <div class="form-group">
                <label class="form-label" for="module">Module / Chapter</label>
                <input type="text" id="module" name="module" class="form-control"
                       value="<c:out value='${module}' />" placeholder="e.g. Module 3, Unit 2" />
            </div>
        </div>

        <div class="form-group">
            <label class="form-label" for="file">Select Document / Note File</label>
            <input type="file" id="file" name="file" class="form-control"
                   accept=".pdf,.doc,.docx,.ppt,.pptx,.txt,.png,.jpg,.jpeg" required />
            <div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 0.35rem;">
                Supported: PDF, Word (DOC/DOCX), PowerPoint (PPT/PPTX), Plain Text (TXT), Images (PNG/JPG). Max size: 10 MB.
            </div>
        </div>

        <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 1rem;">
            📤 Upload & Publish Note
        </button>
    </form>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
