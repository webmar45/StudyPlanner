param(
    [string]$Uri = $env:MONGODB_URI
)

if (-not $Uri) {
    Write-Error "Set MONGODB_URI or pass -Uri. Example: mongodb+srv://user:pass@cluster.mongodb.net/smart_study_planner"
    exit 1
}

$mongosh = Get-Command mongosh -ErrorAction SilentlyContinue
if (-not $mongosh) {
    Write-Host "mongosh not found. Install MongoDB Shell or verify URI in Atlas UI (Connect -> Drivers)."
    Write-Host "URI host: $($Uri -replace 'mongodb(\+srv)?://[^@]+@','mongodb://***@' -replace ':([^/@]+)@',':***@')"
    exit 0
}

Write-Host "Pinging MongoDB..."
& mongosh $Uri --eval "db.runCommand({ ping: 1 })" --quiet
if ($LASTEXITCODE -eq 0) {
    Write-Host "MongoDB connection OK."
} else {
    Write-Error "MongoDB connection failed. Check Atlas network access (0.0.0.0/0) and credentials."
    exit 1
}
