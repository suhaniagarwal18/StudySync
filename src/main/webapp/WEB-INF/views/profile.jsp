<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="My Profile & Skills - StudySync" scope="request" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-header">
    <div>
        <h1 class="page-title">My Profile & Academic Skills</h1>
        <p class="page-subtitle">Configure the subjects you can teach and the topics you wish to learn.</p>
    </div>
</div>

<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 2rem;">
    <!-- Profile Information Card -->
    <div class="card">
        <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 1rem; color: var(--secondary);">
            Personal Information
        </h2>

        <form action="<c:url value='/profile' />" method="post">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />

            <div class="form-group">
                <label class="form-label" for="name">Full Name</label>
                <input type="text" id="name" name="name" class="form-control"
                       value="<c:out value='${profile.name}' />" required />
            </div>

            <div class="form-group">
                <label class="form-label" for="email">Email Address</label>
                <input type="email" id="email" class="form-control"
                       value="<c:out value='${profile.email}' />" disabled />
                <span style="font-size: 0.75rem; color: var(--text-muted);">University email cannot be changed.</span>
            </div>

            <div class="form-group">
                <label class="form-label" for="department">Academic Department</label>
                <input type="text" id="department" name="department" class="form-control"
                       value="<c:out value='${profile.department}' />" required />
            </div>

            <div class="form-group">
                <label class="form-label" for="bio">Bio & Academic Interests</label>
                <textarea id="bio" name="bio" rows="4" class="form-control"
                          placeholder="Tell potential study buddies about your strengths and study habits..."><c:out value="${profile.bio}" /></textarea>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%;">
                Save Profile Changes
            </button>
        </form>
    </div>

    <!-- Skills Management Card -->
    <div>
        <div class="card" style="margin-bottom: 1.5rem;">
            <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 1rem; color: var(--secondary);">
                Add a Skill
            </h2>

            <form action="<c:url value='/skills' />" method="post">
                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                <input type="hidden" name="action" value="add" />

                <div class="form-group">
                    <label class="form-label" for="skillName">Skill or Subject Name</label>
                    <input type="text" id="skillName" name="skillName" class="form-control"
                           list="suggestedSkills" placeholder="e.g. Java, DBMS, DSA, Python..." required />
                    <datalist id="suggestedSkills">
                        <c:forEach var="s" items="${allSkills}">
                            <option value="<c:out value='${s}' />" />
                        </c:forEach>
                    </datalist>
                </div>

                <div class="form-group">
                    <label class="form-label">Category</label>
                    <div style="display: flex; gap: 1.5rem; margin-top: 0.25rem;">
                        <label style="font-size: 0.9rem; cursor: pointer;">
                            <input type="radio" name="skillType" value="KNOWN" checked />
                            <strong>Skills I Know</strong> (Can tutor)
                        </label>
                        <label style="font-size: 0.9rem; cursor: pointer;">
                            <input type="radio" name="skillType" value="NEEDED" />
                            <strong>Skills I Need</strong> (Want to learn)
                        </label>
                    </div>
                </div>

                <button type="submit" class="btn btn-success" style="width: 100%;">
                    + Add to My Skills
                </button>
            </form>
        </div>

        <!-- Current Skills Display & Removal -->
        <div class="card">
            <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 1rem; color: var(--secondary);">
                Current Active Skills
            </h2>

            <div style="margin-bottom: 1.25rem;">
                <h3 style="font-size: 0.9rem; font-weight: 600; color: var(--text-muted); text-transform: uppercase; margin-bottom: 0.5rem;">
                    Skills I Know (Can Tutor):
                </h3>
                <c:choose>
                    <c:when test="${not empty student.skillsKnown}">
                        <div style="display: flex; flex-wrap: wrap; gap: 0.35rem;">
                            <c:forEach var="skill" items="${student.skillsKnown}">
                                <span class="badge badge-skill">
                                    <c:out value="${skill}" />
                                    <form action="<c:url value='/skills' />" method="post" style="display: inline; margin: 0;">
                                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                                        <input type="hidden" name="action" value="remove" />
                                        <input type="hidden" name="skillName" value="<c:out value='${skill}' />" />
                                        <input type="hidden" name="skillType" value="KNOWN" />
                                        <button type="submit" style="background: none; border: none; cursor: pointer; color: var(--primary-dark); font-weight: bold; margin-left: 0.25rem;" title="Remove skill">&times;</button>
                                    </form>
                                </span>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <p style="font-size: 0.85rem; color: var(--text-muted);">No known skills added yet.</p>
                    </c:otherwise>
                </c:choose>
            </div>

            <div>
                <h3 style="font-size: 0.9rem; font-weight: 600; color: var(--text-muted); text-transform: uppercase; margin-bottom: 0.5rem;">
                    Skills I Need (Seeking Help):
                </h3>
                <c:choose>
                    <c:when test="${not empty student.skillsNeeded}">
                        <div style="display: flex; flex-wrap: wrap; gap: 0.35rem;">
                            <c:forEach var="skill" items="${student.skillsNeeded}">
                                <span class="badge badge-skill badge-needed">
                                    <c:out value="${skill}" />
                                    <form action="<c:url value='/skills' />" method="post" style="display: inline; margin: 0;">
                                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                                        <input type="hidden" name="action" value="remove" />
                                        <input type="hidden" name="skillName" value="<c:out value='${skill}' />" />
                                        <input type="hidden" name="skillType" value="NEEDED" />
                                        <button type="submit" style="background: none; border: none; cursor: pointer; color: #9d174d; font-weight: bold; margin-left: 0.25rem;" title="Remove skill">&times;</button>
                                    </form>
                                </span>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <p style="font-size: 0.85rem; color: var(--text-muted);">No needed skills added yet.</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
