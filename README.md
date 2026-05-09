# Smart Study Planner & Focus Tracker

A production-style full-stack web app for students to plan tasks, track Pomodoro focus sessions, and view productivity analytics.

## Tech Stack
- Frontend: React + Tailwind CSS + Recharts
- Backend: Spring Boot + Spring Security + JWT
- Database: MongoDB

## Features
- JWT authentication (register/login, BCrypt passwords)
- Study task CRUD with priority, duration, and completion tracking
- Filters by date, subject, and priority
- Pomodoro timer (25/5) with session start/end persistence
- Analytics dashboard (daily/weekly totals, task stats, trends, activity + **task-linked** breakdown)
- Optional date on analytics: `GET /api/analytics?date=YYYY-MM-DD`
- Rule-based **next-task recommendations**: `GET /api/recommendations/next-tasks`
- **LLM-style** weekly summary: `GET /api/llm/weekly-summary` (template by default; set `app.openai.api-key` for OpenAI)
- **Quick-add parsing**: `POST /api/llm/quick-add` `{ "text": "..." }`
- Reminder endpoint for pending tasks
- Landing, auth, dashboard, planner, timer, and analytics pages
- Layered backend architecture (controller/service/repository) + DTOs

## Project Structure
- `backend`: Spring Boot API
- `frontend`: React app
- `DATABASE_SCHEMA.md`: MongoDB schema
- `postman_collection.json`: sample API collection

## Run Backend
1. Install Java 17+, Maven, MongoDB.
2. Start MongoDB locally at `mongodb://localhost:27017`.
3. From `backend` folder:
   - `mvn spring-boot:run`
4. API base URL: `http://localhost:8080/api`

## Run Frontend
1. From `frontend` folder:
   - `npm install`
   - `npm run dev`
2. App URL: `http://localhost:5173`

## Important Backend Config
Edit `backend/src/main/resources/application.properties` if needed:
- `spring.data.mongodb.uri`
- `app.jwt.secret`
- `app.jwt.expiration-ms`
- `app.openai.api-key` (optional)
- `app.openai.model` (optional, default `gpt-4o-mini`)

## API Quick Start
- Register: `POST /api/auth/register`
- Login: `POST /api/auth/login`
- Tasks: `GET/POST/PUT/DELETE /api/tasks`
- Toggle task complete: `PATCH /api/tasks/{taskId}/complete`
- Start session: `POST /api/session/start`
- End session: `POST /api/session/end`
- Analytics: `GET /api/analytics` (optional `?date=`)
- Reminder: `GET /api/notifications/reminder`
- Recommendations: `GET /api/recommendations/next-tasks?limit=5`
- Weekly insight: `GET /api/llm/weekly-summary`
- Quick-add parse: `POST /api/llm/quick-add`

Use `postman_collection.json` to import ready-to-use examples.
