param(
    [Parameter(Mandatory = $true)]
    [string]$ApiBaseUrl,
    [string]$Email = "smoke-$(Get-Random)@example.com",
    [string]$Password = "SmokeTest1!",
    [string]$Name = "Smoke Test"
)

$ErrorActionPreference = "Stop"
$base = $ApiBaseUrl.TrimEnd("/")

Write-Host "Health: GET $base/health"
$health = Invoke-RestMethod -Uri "$base/health" -Method Get
Write-Host $health

Write-Host "Register: POST $base/auth/register"
$reg = Invoke-RestMethod -Uri "$base/auth/register" -Method Post -ContentType "application/json" -Body (@{
    name = $Name
    email = $Email
    password = $Password
} | ConvertTo-Json)
if (-not $reg.token) { throw "Register did not return token" }
Write-Host "Register OK"

$headers = @{ Authorization = "Bearer $($reg.token)" }

Write-Host "Create task: POST $base/tasks"
$task = Invoke-RestMethod -Uri "$base/tasks" -Method Post -Headers $headers -ContentType "application/json" -Body (@{
    title = "Smoke task"
    subject = "Math"
    date = (Get-Date).ToString("yyyy-MM-dd")
    duration = 25
    priority = "MEDIUM"
    status = "PENDING"
} | ConvertTo-Json)
Write-Host "Task id: $($task.id)"

Write-Host "Analytics: GET $base/analytics"
$null = Invoke-RestMethod -Uri "$base/analytics" -Method Get -Headers $headers

Write-Host "All smoke checks passed."
