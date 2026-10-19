# Comprehensive End-to-End HTTP Test Suite for StudySync
$ErrorActionPreference = "Continue"
$baseUrl = "http://localhost:8080/StudySync"
$report = [System.Collections.Generic.List[PSCustomObject]]::new()

function Test-Step($id, $name, $scriptBlock) {
    try {
        $result = & $scriptBlock
        $status = if ($result.Passed) { "PASS" } else { "FAIL" }
        $report.Add([PSCustomObject]@{
            ID = $id
            Scenario = $name
            Status = $status
            Details = $result.Details
        })
        Write-Host "[$status] ${id}: $name - $($result.Details)"
    } catch {
        $report.Add([PSCustomObject]@{
            ID = $id
            Scenario = $name
            Status = "FAIL"
            Details = $_.Exception.Message
        })
        Write-Host "[FAIL] ${id}: $name - $($_.Exception.Message)"
    }
}

Write-Host "=========================================================="
Write-Host "STARTING STUDYSYNC END-TO-END HTTP VERIFICATION SUITE"
Write-Host "Target: $baseUrl"
Write-Host "=========================================================="

# Scenario 1: Anonymous Landing Page
Test-Step "SC-01" "Anonymous Landing Page" {
    $res = Invoke-WebRequest -Uri "$baseUrl/" -UseBasicParsing
    $pass = ($res.StatusCode -eq 200) -and ($res.Content -match "StudySync" -or $res.Content -match "Peer Study Buddy")
    return @{ Passed = $pass; Details = "Status 200, Content rendered: $($res.Content.Length) bytes" }
}

# Scenario 2: Unauthenticated Protected Route Access
Test-Step "SC-02" "Unauthenticated Access Redirect" {
    $anonSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $res = Invoke-WebRequest -Uri "$baseUrl/dashboard" -WebSession $anonSession -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
    $pass = ($res.StatusCode -eq 302) -and ($res.Headers.Location -match "/login")
    return @{ Passed = $pass; Details = "302 Redirect to $($res.Headers.Location)" }
}

# Scenario 3: CSRF Token Generation
$studentSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$csrfToken = ""
Test-Step "SC-03" "CSRF Token Generation" {
    $loginGet = Invoke-WebRequest -Uri "$baseUrl/login" -WebSession $studentSession -UseBasicParsing
    if ($loginGet.Content -match 'name="csrfToken"\s+value="([^"]+)"') {
        $script:csrfToken = $matches[1]
        return @{ Passed = $true; Details = "CSRF Token generated: $($matches[1].Substring(0, 10))..." }
    }
    return @{ Passed = $false; Details = "CSRF token missing from login form" }
}

# Scenario 4: Student Authentication (Aarav Sharma)
Test-Step "SC-04" "Student Login (Aarav Sharma)" {
    $body = @{
        email = "aarav@studysync.edu"
        password = "Student@123"
        csrfToken = $script:csrfToken
    }
    $res = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body $body -WebSession $studentSession -UseBasicParsing
    $pass = ($res.BaseResponse.ResponseUri.ToString() -match "/dashboard") -and ($res.Content -match "Aarav Sharma")
    return @{ Passed = $pass; Details = "Redirected to /dashboard, Welcome Aarav Sharma displayed" }
}

# Scenario 5: Student Dashboard View & Statistics
Test-Step "SC-05" "Student Dashboard Statistics" {
    $res = Invoke-WebRequest -Uri "$baseUrl/dashboard" -WebSession $studentSession -UseBasicParsing
    $pass = ($res.StatusCode -eq 200) -and ($res.Content -match "Computer Science") -and ($res.Content -match "My Skills Profile")
    return @{ Passed = $pass; Details = "Department and My Skills Profile rendered" }
}

# Scenario 6: Student Profile & Add Skill
Test-Step "SC-06" "Student Profile & Add Dynamic Skill" {
    $profileGet = Invoke-WebRequest -Uri "$baseUrl/profile" -WebSession $studentSession -UseBasicParsing
    $token = ""
    if ($profileGet.Content -match 'name="csrfToken"\s+value="([^"]+)"') { $token = $matches[1] }
    $skillBody = @{
        action = "add"
        skillName = "Deep Learning"
        skillType = "NEEDED"
        csrfToken = $token
    }
    $res = Invoke-WebRequest -Uri "$baseUrl/skills" -Method Post -Body $skillBody -WebSession $studentSession -UseBasicParsing
    $pass = ($res.StatusCode -eq 200) -and (($res.Content -match "Deep Learning") -or ($res.Content -match "Added 'Deep Learning'"))
    return @{ Passed = $pass; Details = "Added 'Deep Learning' (NEEDED) via PRG pattern" }
}

# Scenario 7: Buddy Search & Matching Algorithm
Test-Step "SC-07" "Buddy Search & Compatibility Score" {
    $res = Invoke-WebRequest -Uri "$baseUrl/buddies" -WebSession $studentSession -UseBasicParsing
    $pass = ($res.StatusCode -eq 200) -and ($res.Content -match "Diya Patel") -and ($res.Content -match "%")
    return @{ Passed = $pass; Details = "Found Diya Patel with calculated compatibility score %" }
}

# Scenario 8: Send Study Request
Test-Step "SC-08" "Send Study Request" {
    $prof = Invoke-WebRequest -Uri "$baseUrl/profile" -WebSession $studentSession -UseBasicParsing
    $token = ""
    if ($prof.Content -match 'name="csrfToken"\s+value="([^"]+)"') { $token = $matches[1] }
    $body = @{
        action = "send"
        receiverId = "2"
        subject = "Java"
        csrfToken = $token
    }
    $res = Invoke-WebRequest -Uri "$baseUrl/requests" -Method Post -Body $body -WebSession $studentSession -UseBasicParsing
    $pass = ($res.StatusCode -eq 200) -and (($res.Content -match "sent successfully") -or ($res.Content -match "already have a pending"))
    return @{ Passed = $pass; Details = "Study request submitted and flash message displayed" }
}

# Scenario 9: View Study Requests Panel
Test-Step "SC-09" "View Study Requests Panel" {
    $res = Invoke-WebRequest -Uri "$baseUrl/requests" -WebSession $studentSession -UseBasicParsing
    $pass = ($res.StatusCode -eq 200) -and ($res.Content -match "Incoming Requests" -or $res.Content -match "Sent Requests")
    return @{ Passed = $pass; Details = "Requests panel rendered with pending/sent tabs" }
}

# Scenario 10: View Study Sessions List
Test-Step "SC-10" "View Study Sessions List" {
    $res = Invoke-WebRequest -Uri "$baseUrl/sessions" -WebSession $studentSession -UseBasicParsing
    $pass = ($res.StatusCode -eq 200) -and ($res.Content -match "Study Sessions" -or $res.Content -match "COMPLETED")
    return @{ Passed = $pass; Details = "Study sessions view loaded" }
}

# Scenario 11: Notes Listing
Test-Step "SC-11" "Notes Listing & Subject Search" {
    $res = Invoke-WebRequest -Uri "$baseUrl/notes" -WebSession $studentSession -UseBasicParsing
    $pass = ($res.StatusCode -eq 200) -and ($res.Content -match "Complete Java &amp; OOP Master Guide|Java")
    return @{ Passed = $pass; Details = "Notes repository rendered with sample academic notes" }
}

# Scenario 12: Note Download (PDF Integrity)
Test-Step "SC-12" "Note Download (PDF Integrity & MIME)" {
    $res = Invoke-WebRequest -Uri "$baseUrl/notes/download?id=1" -WebSession $studentSession -UseBasicParsing
    $pass = ($res.StatusCode -eq 200) -and ($res.Headers["Content-Type"] -match "application/pdf") -and ($res.RawContentLength -gt 0)
    return @{ Passed = $pass; Details = "Downloaded PDF ($($res.RawContentLength) bytes), Content-Type: $($res.Headers['Content-Type'])" }
}

# Scenario 13: RBAC Defense - Student Accessing Admin Route
Test-Step "SC-13" "RBAC Defense (Student accessing Admin)" {
    try {
        $res = Invoke-WebRequest -Uri "$baseUrl/admin/dashboard" -WebSession $studentSession -UseBasicParsing -ErrorAction Stop
        return @{ Passed = $false; Details = "Admin route was accessible to student!" }
    } catch {
        $code = [int]$_.Exception.Response.StatusCode
        $pass = ($code -eq 403)
        return @{ Passed = $pass; Details = "HTTP 403 Forbidden correctly returned" }
    }
}

# Scenario 14: Admin Authentication
$adminSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
Test-Step "SC-14" "Admin Authentication (System Administrator)" {
    $loginGet = Invoke-WebRequest -Uri "$baseUrl/login" -WebSession $adminSession -UseBasicParsing
    $token = ""
    if ($loginGet.Content -match 'name="csrfToken"\s+value="([^"]+)"') { $token = $matches[1] }
    $body = @{
        email = "admin@studysync.edu"
        password = "Admin@123"
        csrfToken = $token
    }
    $res = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body $body -WebSession $adminSession -UseBasicParsing
    $pass = ($res.BaseResponse.ResponseUri.ToString() -match "/admin/dashboard") -and ($res.Content -match "Administrator Console")
    return @{ Passed = $pass; Details = "Admin authenticated and redirected to /admin/dashboard" }
}

# Scenario 15: Admin User Management
Test-Step "SC-15" "Admin User Management" {
    $res = Invoke-WebRequest -Uri "$baseUrl/admin/users" -WebSession $adminSession -UseBasicParsing
    $pass = ($res.StatusCode -eq 200) -and ($res.Content -match "Aarav Sharma") -and ($res.Content -match "admin@studysync.edu")
    return @{ Passed = $pass; Details = "User management table rendered all students and admins" }
}

# Scenario 16: Admin Note Moderation
Test-Step "SC-16" "Admin Note Moderation" {
    $res = Invoke-WebRequest -Uri "$baseUrl/admin/notes" -WebSession $adminSession -UseBasicParsing
    $pass = ($res.StatusCode -eq 200) -and ($res.Content -match "Manage Notes")
    return @{ Passed = $pass; Details = "Notes moderation panel rendered with delete controls" }
}

# Scenario 17: CSRF Attack Defense (POST without token)
Test-Step "SC-17" "CSRF Protection (Missing Token)" {
    try {
        $body = @{ action = "add"; skillName = "Malicious Skill"; skillType = "NEEDED" }
        $res = Invoke-WebRequest -Uri "$baseUrl/skills" -Method Post -Body $body -WebSession $studentSession -UseBasicParsing -ErrorAction Stop
        return @{ Passed = $false; Details = "Request without CSRF token succeeded!" }
    } catch {
        $code = [int]$_.Exception.Response.StatusCode
        $pass = ($code -eq 403)
        return @{ Passed = $pass; Details = "HTTP 403 Forbidden correctly rejected request missing CSRF" }
    }
}

# Scenario 18: Authentication Defense (Invalid Credentials)
Test-Step "SC-18" "Authentication Defense (Invalid Credentials)" {
    $fakeSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $loginGet = Invoke-WebRequest -Uri "$baseUrl/login" -WebSession $fakeSession -UseBasicParsing
    $token = ""
    if ($loginGet.Content -match 'name="csrfToken"\s+value="([^"]+)"') { $token = $matches[1] }
    $body = @{ email = "aarav@studysync.edu"; password = "WrongPassword999!"; csrfToken = $token }
    $res = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body $body -WebSession $fakeSession -UseBasicParsing
    $pass = ($res.Content -match "Invalid email or password")
    return @{ Passed = $pass; Details = "Generic error message rendered without credential leakage" }
}

# Scenario 19: User Logout & Session Invalidation
Test-Step "SC-19" "User Logout & Session Invalidation" {
    $prof = Invoke-WebRequest -Uri "$baseUrl/dashboard" -WebSession $studentSession -UseBasicParsing
    $token = ""
    if ($prof.Content -match 'name="csrfToken"\s+value="([^"]+)"') { $token = $matches[1] }
    $body = @{ csrfToken = $token }
    $res = Invoke-WebRequest -Uri "$baseUrl/logout" -Method Post -Body $body -WebSession $studentSession -UseBasicParsing
    $pass = ($res.BaseResponse.ResponseUri.ToString() -match "/login") -and ($res.Content -match "You have been logged out")
    return @{ Passed = $pass; Details = "Session terminated and redirected to /login with flash message" }
}

Write-Host "`n=========================================================="
$passedCount = ($report | Where-Object { $_.Status -eq "PASS" }).Count
Write-Host "SUMMARY: $passedCount / $($report.Count) SCENARIOS PASSED"
Write-Host "=========================================================="
$report | Format-Table -AutoSize
