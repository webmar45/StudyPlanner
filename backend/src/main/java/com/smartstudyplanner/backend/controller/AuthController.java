package com.smartstudyplanner.backend.controller;

import com.smartstudyplanner.backend.dto.auth.AuthResponse;
import com.smartstudyplanner.backend.dto.auth.LoginRequest;
import com.smartstudyplanner.backend.dto.auth.RegisterRequest;
import com.smartstudyplanner.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/health")
    public String health() {
        return "Smart Study Planner backend is running";
    }
}
