package com.smartstudyplanner.backend.controller;

import com.smartstudyplanner.backend.dto.learning.LearningResourcesResponse;
import com.smartstudyplanner.backend.dto.task.TaskRequest;
import com.smartstudyplanner.backend.model.StudyTask;
import com.smartstudyplanner.backend.model.enums.Priority;
import com.smartstudyplanner.backend.service.LearningResourcesService;
import com.smartstudyplanner.backend.service.TaskService;
import com.smartstudyplanner.backend.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    private final LearningResourcesService learningResourcesService;
    private final AuthUtil authUtil;

    public TaskController(
            TaskService taskService,
            LearningResourcesService learningResourcesService,
            AuthUtil authUtil
    ) {
        this.taskService = taskService;
        this.learningResourcesService = learningResourcesService;
        this.authUtil = authUtil;
    }

    @PostMapping
    public StudyTask createTask(@Valid @RequestBody TaskRequest request, Authentication authentication) {
        String userId = authUtil.getUserId(authentication);
        return taskService.createTask(userId, request);
    }

    @GetMapping
    public List<StudyTask> getTasks(
            Authentication authentication,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) Priority priority
    ) {
        String userId = authUtil.getUserId(authentication);
        return taskService.getTasks(userId, date, subject, priority);
    }

    @PutMapping("/{taskId}")
    public StudyTask updateTask(
            @PathVariable String taskId,
            @Valid @RequestBody TaskRequest request,
            Authentication authentication
    ) {
        String userId = authUtil.getUserId(authentication);
        return taskService.updateTask(userId, taskId, request);
    }

    @PatchMapping("/{taskId}/complete")
    public StudyTask toggleComplete(
            @PathVariable String taskId,
            @RequestBody Map<String, Boolean> payload,
            Authentication authentication
    ) {
        String userId = authUtil.getUserId(authentication);
        boolean completed = payload.getOrDefault("completed", false);
        return taskService.toggleComplete(userId, taskId, completed);
    }

    @DeleteMapping("/{taskId}")
    public void deleteTask(@PathVariable String taskId, Authentication authentication) {
        String userId = authUtil.getUserId(authentication);
        taskService.deleteTask(userId, taskId);
    }

    @GetMapping("/{taskId}/learning-resources")
    public LearningResourcesResponse learningResources(
            @PathVariable String taskId,
            Authentication authentication
    ) {
        String userId = authUtil.getUserId(authentication);
        return learningResourcesService.getResourcesForTask(userId, taskId);
    }
}
