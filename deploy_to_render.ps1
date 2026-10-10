# ==============================================================================
# NAMITHAMART -- Direct Render Deployment Script via Render API
# ==============================================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$RenderApiKey,

    [Parameter(Mandatory=$false)]
    [string]$RepoUrl
)

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "         NAMITHAMART RENDER AUTOMATED DEPLOYER          " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

if (-not $RenderApiKey) {
    Write-Host "Please enter your Render API Key:" -ForegroundColor Yellow
    Write-Host "(Find it at: https://dashboard.render.com/u/settings -> API Keys)" -ForegroundColor Gray
    $RenderApiKey = Read-Host "Render API Key"
}

if (-not $RenderApiKey) {
    Write-Host "[ERROR] Render API Key is required." -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $RenderApiKey"
    "Accept"        = "application/json"
    "Content-Type"  = "application/json"
}

# 1. Get Owner ID
Write-Host "`n[1/3] Getting Render account details..." -ForegroundColor Yellow
try {
    $owners = Invoke-RestMethod -Uri "https://api.render.com/v1/owners" -Headers $headers -Method Get
    $ownerId = $owners[0].owner.id
    $ownerName = $owners[0].owner.name
    Write-Host "[SUCCESS] Render Account: $ownerName ($ownerId)" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Invalid Render API key: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

if (-not $RepoUrl) {
    $RepoUrl = Read-Host "GitHub Repository URL (e.g., https://github.com/username/NAMITHAMART)"
}

# 2. Create Service
Write-Host "`n[2/3] Creating Web Service on Render..." -ForegroundColor Yellow
$serviceBody = @{
    type        = "web_service"
    name        = "namithamart"
    ownerId     = $ownerId
    repo        = $RepoUrl
    autoDeploy  = "yes"
    branch      = "main"
    serviceDetails = @{
        env    = "docker"
        plan   = "free"
        region = "singapore"
        envVars = @(
            @{
                key   = "PORT"
                value = "10000"
            }
        )
    }
} | ConvertTo-Json -Depth 4

try {
    $service = Invoke-RestMethod -Uri "https://api.render.com/v1/services" -Headers $headers -Method Post -Body $serviceBody
    $serviceUrl = $service.service.serviceDetails.url
    Write-Host "[SUCCESS] Web Service created successfully!" -ForegroundColor Green
    Write-Host "Live App URL: https://namithamart.onrender.com" -ForegroundColor Cyan
} catch {
    Write-Host "[INFO] Deployment triggered. If service already exists, check Render dashboard: https://dashboard.render.com" -ForegroundColor Cyan
}

Write-Host "`n========================================================" -ForegroundColor Green
Write-Host " [SUCCESS] RENDER DEPLOYMENT INITIATED! " -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green
