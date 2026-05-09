package com.smartstudyplanner.backend.dto.notification;

public record ReminderResponse(
        String message,
        boolean hasUpcomingTask
) {}
