package com.smartstudyplanner.backend.controller;

import com.smartstudyplanner.backend.dto.analytics.AnalyticsResponse;
import com.smartstudyplanner.backend.dto.notification.ReminderResponse;
import com.smartstudyplanner.backend.model.StudyTask;
import com.smartstudyplanner.backend.model.enums.TaskStatus;
import com.smartstudyplanner.backend.service.AnalyticsService;
import com.smartstudyplanner.backend.service.TaskService;
import com.smartstudyplanner.backend.util.AuthUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AnalyticsController {
    private final AnalyticsService analyticsService;
    private final TaskService taskService;
    private final AuthUtil authUtil;

    public AnalyticsController(AnalyticsService analyticsService, TaskService taskService, AuthUtil authUtil) {
        this.analyticsService = analyticsService;
        this.taskService = taskService;
        this.authUtil = authUtil;
    }

    @GetMapping("/analytics")
    public AnalyticsResponse getAnalytics(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String userId = authUtil.getUserId(authentication);
        return analyticsService.getAnalytics(userId, date);
    }

    @GetMapping("/notifications/reminder")
    public ReminderResponse getReminder(Authentication authentication) {
        String userId = authUtil.getUserId(authentication);
        List<StudyTask> tasks = taskService.getTasks(userId, LocalDate.now().toString(), null, null);
        boolean hasUpcoming = tasks.stream().anyMatch(task -> task.getStatus() == TaskStatus.PENDING);
        String message = hasUpcoming
                ? "You have pending study tasks today. Keep your streak alive!"
                : "No pending tasks today. Plan your next learning sprint.";
        return new ReminderResponse(message, hasUpcoming);
    }
}
