package com.smartstudyplanner.backend.dto.session;

public record SessionActionResponse(
        String message,
        String sessionId,
        Integer duration
) {}
