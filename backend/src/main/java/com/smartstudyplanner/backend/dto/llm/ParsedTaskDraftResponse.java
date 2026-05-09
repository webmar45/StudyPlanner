package com.smartstudyplanner.backend.dto.llm;

import com.smartstudyplanner.backend.model.enums.Priority;
import com.smartstudyplanner.backend.model.enums.TaskStatus;

import java.time.LocalDate;

public record ParsedTaskDraftResponse(
        String title,
        String subject,
        LocalDate date,
        int duration,
        Priority priority,
        TaskStatus status
) {}
