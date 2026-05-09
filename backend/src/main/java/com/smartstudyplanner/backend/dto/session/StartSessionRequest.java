package com.smartstudyplanner.backend.dto.session;

public record StartSessionRequest(
        String activityName,
        String taskId
) {}
