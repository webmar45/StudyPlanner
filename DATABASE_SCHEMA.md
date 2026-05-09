# MongoDB Schema

## `users`
- `_id`: `ObjectId/String`
- `name`: `String`
- `email`: `String` (unique index)
- `password`: `String` (BCrypt hash)

## `tasks`
- `_id`: `ObjectId/String`
- `userId`: `String`
- `title`: `String`
- `subject`: `String`
- `date`: `Date`
- `duration`: `Number`
- `priority`: `LOW | MEDIUM | HIGH`
- `status`: `PENDING | COMPLETED`
- `createdAt`: `DateTime`

## `study_sessions`
- `_id`: `ObjectId/String`
- `userId`: `String`
- `activityName`: `String` (display label for focus block)
- `taskId`: `String` (optional; links to `tasks._id` when timer is tied to a task)
- `startTime`: `DateTime`
- `endTime`: `DateTime`
- `duration`: `Number`
