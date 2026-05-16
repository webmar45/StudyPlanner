param(
    [Parameter(Mandatory = $true)]
    [string]$ApiBaseUrl
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$frontend = Join-Path $root "frontend"

$env:VITE_API_BASE_URL = if ($ApiBaseUrl.EndsWith("/api")) { $ApiBaseUrl } else { "$($ApiBaseUrl.TrimEnd('/'))/api" }
Write-Host "Building frontend with VITE_API_BASE_URL=$env:VITE_API_BASE_URL"
Push-Location $frontend
npm run build
Pop-Location

Write-Host "Deploying to Cloudflare Pages (requires: npx wrangler login)"
npx --yes wrangler pages deploy "$frontend/dist" --project-name=smart-study-planner --branch=main
