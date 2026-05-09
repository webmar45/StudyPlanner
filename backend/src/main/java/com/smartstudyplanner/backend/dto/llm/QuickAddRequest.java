package com.smartstudyplanner.backend.dto.llm;

import jakarta.validation.constraints.NotBlank;

public record QuickAddRequest(
        @NotBlank String text
) {}
