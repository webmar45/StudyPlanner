# Opens MongoDB Atlas free-tier signup and prints connection-string checklist.
Write-Host @"
MongoDB Atlas setup (free M0)
=============================
1. Create cluster (M0, any region)
2. Security -> Database Access -> Add user (password auth)
3. Security -> Network Access -> Add IP Address -> Allow Access from Anywhere (0.0.0.0/0)
4. Database -> Connect -> Drivers -> copy connection string
5. Replace <password> and set database: smart_study_planner

Example:
  mongodb+srv://USER:PASS@cluster0.xxxxx.mongodb.net/smart_study_planner?retryWrites=true&w=majority

Paste into Render as MONGODB_URI when deploying the blueprint.
"@

Start-Process "https://www.mongodb.com/cloud/atlas/register"
