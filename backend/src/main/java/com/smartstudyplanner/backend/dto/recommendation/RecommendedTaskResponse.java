package com.smartstudyplanner.backend.dto.recommendation;

import java.time.LocalDate;

public record RecommendedTaskResponse(
        String taskId,
        String title,
        String subject,
        LocalDate date,
        String priority,
        String reason
) {}
