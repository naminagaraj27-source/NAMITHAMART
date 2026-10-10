# ==============================================================================
# NAMITHAMART -- Direct GitHub Uploader Script (No Git Installation Required!)
# Uses GitHub REST API v3 to create repository and push all project files
# ==============================================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$GitHubToken,

    [Parameter(Mandatory=$false)]
    [string]$RepoName = "NAMITHAMART",

    [Parameter(Mandatory=$false)]
    [switch]$Private = $false
)

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "         NAMITHAMART GITHUB AUTOMATED PUSHER            " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

if (-not $GitHubToken) {
    Write-Host "Please enter your GitHub Personal Access Token (classic with 'repo' scope):" -ForegroundColor Yellow
    Write-Host "(Generate one at: https://github.com/settings/tokens/new)" -ForegroundColor Gray
    $GitHubToken = Read-Host "GitHub Token"
}

if (-not $GitHubToken) {
    Write-Host "[ERROR] GitHub token is required." -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $GitHubToken"
    "Accept"        = "application/vnd.github.v3+json"
    "User-Agent"    = "NAMITHAMART-Deployer"
}

# 1. Verify User
Write-Host "`n[1/5] Authenticating with GitHub..." -ForegroundColor Yellow
try {
    $user = Invoke-RestMethod -Uri "https://api.github.com/user" -Headers $headers -Method Get
    $username = $user.login
    Write-Host "[SUCCESS] Logged in as: $username" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Failed to authenticate with GitHub. Check your token." -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

# 2. Create or Find Repository
Write-Host "`n[2/5] Creating / Verifying repository '$RepoName'..." -ForegroundColor Yellow
$repoExists = $false
try {
    $repo = Invoke-RestMethod -Uri "https://api.github.com/repos/$username/$RepoName" -Headers $headers -Method Get
    Write-Host "[INFO] Repository '$RepoName' already exists." -ForegroundColor Cyan
    $repoExists = $true
} catch {
    $repoBody = @{
        name        = $RepoName
        description = "NAMITHAMART - Complete Full-Stack Multi-Seller E-Commerce Platform"
        private     = $Private.IsPresent
        auto_init   = $true
    } | ConvertTo-Json

    try {
        $repo = Invoke-RestMethod -Uri "https://api.github.com/user/repos" -Headers $headers -Method Post -Body $repoBody
        Write-Host "[SUCCESS] Created new repository: https://github.com/$username/$RepoName" -ForegroundColor Green
        Start-Sleep -Seconds 3
    } catch {
        Write-Host "[ERROR] Could not create repository: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

# 3. Read Files to Upload
Write-Host "`n[3/5] Scanning files to push..." -ForegroundColor Yellow
$currentPath = (Get-Location).Path
$files = Get-ChildItem -Path . -Recurse -File | Where-Object {
    $rel = $_.FullName.Substring($currentPath.Length + 1).Replace("\", "/")
    $rel -notmatch '^bin/' -and
    $rel -notmatch '\.db' -and
    $rel -notmatch '\.log$' -and
    $rel -notmatch '^mingit'
}

Write-Host "Found $($files.Count) project files ready for upload." -ForegroundColor Cyan

# 4. Upload blobs and build tree
Write-Host "`n[4/5] Uploading files to GitHub..." -ForegroundColor Yellow
$treeItems = @()

foreach ($file in $files) {
    $relPath = $file.FullName.Substring($currentPath.Length + 1).Replace("\", "/")
    
    $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
    $base64 = [Convert]::ToBase64String($bytes)

    $blobBody = @{
        content  = $base64
        encoding = "base64"
    } | ConvertTo-Json

    try {
        $blobResp = Invoke-RestMethod -Uri "https://api.github.com/repos/$username/$RepoName/git/blobs" -Headers $headers -Method Post -Body $blobBody
        $treeItems += @{
            path = $relPath
            mode = "100644"
            type = "blob"
            sha  = $blobResp.sha
        }
        Write-Host "  Uploaded: $relPath" -ForegroundColor Gray
    } catch {
        Write-Host "  [WARN] Failed uploading ${relPath}: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 5. Create Tree, Commit and Update Branch
Write-Host "`n[5/5] Finalizing commit and updating main branch..." -ForegroundColor Yellow
$parentSha = $null
try {
    $ref = Invoke-RestMethod -Uri "https://api.github.com/repos/$username/$RepoName/git/refs/heads/main" -Headers $headers -Method Get
    $parentSha = $ref.object.sha
} catch {
    try {
        $ref = Invoke-RestMethod -Uri "https://api.github.com/repos/$username/$RepoName/git/refs/heads/master" -Headers $headers -Method Get
        $parentSha = $ref.object.sha
    } catch {
        $parentSha = $null
    }
}

$treePayload = @{
    tree = $treeItems
}
if ($parentSha) {
    $treePayload["base_tree"] = $parentSha
}
$treeBody = $treePayload | ConvertTo-Json -Depth 5

$treeResp = Invoke-RestMethod -Uri "https://api.github.com/repos/$username/$RepoName/git/trees" -Headers $headers -Method Post -Body $treeBody

$commitPayload = @{
    message = "Initial commit: NAMITHAMART Full-Stack Platform with Purple/Lilac Theme and Render deployment setup"
    tree    = $treeResp.sha
}
if ($parentSha) {
    $commitPayload["parents"] = @($parentSha)
}
$commitBody = $commitPayload | ConvertTo-Json -Depth 3

$commitResp = Invoke-RestMethod -Uri "https://api.github.com/repos/$username/$RepoName/git/commits" -Headers $headers -Method Post -Body $commitBody

# Update main branch ref
$updateRefBody = @{
    sha   = $commitResp.sha
    force = $true
} | ConvertTo-Json

try {
    $null = Invoke-RestMethod -Uri "https://api.github.com/repos/$username/$RepoName/git/refs/heads/main" -Headers $headers -Method Patch -Body $updateRefBody
} catch {
    $createRefBody = @{
        ref = "refs/heads/main"
        sha = $commitResp.sha
    } | ConvertTo-Json
    $null = Invoke-RestMethod -Uri "https://api.github.com/repos/$username/$RepoName/git/refs" -Headers $headers -Method Post -Body $createRefBody
}

Write-Host "`n========================================================" -ForegroundColor Green
Write-Host " [SUCCESS] NAMITHAMART REPOSITORY READY ON GITHUB! " -ForegroundColor Green
Write-Host " Repository URL: https://github.com/$username/$RepoName" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Green
Write-Host "`nNext Step: Host on Render in 2 minutes:" -ForegroundColor Yellow
Write-Host "1. Go to https://dashboard.render.com"
Write-Host "2. Click 'New +' -> 'Web Service'"
Write-Host "3. Connect your repository '$username/$RepoName'"
Write-Host "4. Render will auto-detect the Dockerfile and render.yaml"
Write-Host "5. Click 'Deploy Web Service'!"
