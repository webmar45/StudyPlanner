package com.smartstudyplanner.backend.dto.auth;

public record AuthResponse(
        String token,
        String userId,
        String name,
        String email
) {}
