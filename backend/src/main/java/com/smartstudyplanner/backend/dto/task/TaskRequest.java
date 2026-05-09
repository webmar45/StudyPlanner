package com.smartstudyplanner.backend.dto.task;

import com.smartstudyplanner.backend.model.enums.Priority;
import com.smartstudyplanner.backend.model.enums.TaskStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank String title,
        @NotBlank String subject,
        @NotNull LocalDate date,
        @NotNull @Min(5) Integer duration,
        @NotNull Priority priority,
        @NotNull TaskStatus status
) {}
