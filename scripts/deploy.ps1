# Smart Study Planner — deploy helper (Atlas + Render + Cloudflare Pages)
# Requires: Atlas cluster URI, Render Blueprint, Cloudflare Pages project (see README Deploy section).

param(
    [string]$MongoUri,
    [string]$ApiBaseUrl,
    [string]$FrontendOrigin
)

$root = Split-Path -Parent $PSScriptRoot

Write-Host @"

=== Smart Study Planner deploy checklist ===

1) MongoDB Atlas (free M0)
   - https://www.mongodb.com/cloud/atlas
   - Create cluster, DB user, Network Access: 0.0.0.0/0
   - Connection string -> smart_study_planner database

2) Render (backend)
   - One-click: https://render.com/deploy?repo=https://github.com/webmar45/StudyPlanner
   - Set MONGODB_URI and CORS_ALLOWED_ORIGINS (after step 3)

3) Cloudflare Pages (frontend)
   - Root: frontend | Build: npm install && npm run build | Output: dist
   - Env: VITE_API_BASE_URL=https://YOUR-RENDER-HOST/api

4) CORS: set CORS_ALLOWED_ORIGINS on Render to your Pages URL, redeploy API

"@

if ($MongoUri) {
    $env:MONGODB_URI = $MongoUri
    & "$PSScriptRoot\test-mongodb.ps1" -Uri $MongoUri
}

if ($ApiBaseUrl) {
  $api = $ApiBaseUrl.TrimEnd("/")
  if (-not $api.EndsWith("/api")) { $api = "$api/api" }
  & "$PSScriptRoot\smoke-test.ps1" -ApiBaseUrl $api
}

if ($FrontendOrigin) {
  Write-Host "Set Render env CORS_ALLOWED_ORIGINS=$FrontendOrigin"
}

Write-Host "Done. Push render.yaml to GitHub before using Render Blueprint."
