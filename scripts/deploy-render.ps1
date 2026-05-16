Write-Host @"
Render backend deploy
=====================
1. Open: https://render.com/deploy?repo=https://github.com/webmar45/StudyPlanner
2. Sign in with GitHub and approve the blueprint.
3. When prompted, set:
   - MONGODB_URI = your Atlas connection string
   - CORS_ALLOWED_ORIGINS = your Cloudflare Pages URL (after frontend deploy)
4. Wait for deploy; verify:
   powershell -File scripts/smoke-test.ps1 -ApiBaseUrl "https://YOUR-SERVICE.onrender.com/api"
"@

Start-Process "https://render.com/deploy?repo=https://github.com/webmar45/StudyPlanner"
