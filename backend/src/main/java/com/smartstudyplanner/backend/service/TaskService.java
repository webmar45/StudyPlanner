package com.smartstudyplanner.backend.service;

import com.smartstudyplanner.backend.dto.task.TaskRequest;
import com.smartstudyplanner.backend.exception.ApiException;
import com.smartstudyplanner.backend.model.StudyTask;
import com.smartstudyplanner.backend.model.enums.Priority;
import com.smartstudyplanner.backend.model.enums.TaskStatus;
import com.smartstudyplanner.backend.repository.StudyTaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {
    private final StudyTaskRepository taskRepository;

    public TaskService(StudyTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public StudyTask createTask(String userId, TaskRequest request) {
        StudyTask task = new StudyTask();
        task.setUserId(userId);
        applyRequest(task, request);
        task.setCreatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public List<StudyTask> getTasks(String userId, String date, String subject, Priority priority) {
        if (date != null) {
            return taskRepository.findByUserIdAndDate(userId, java.time.LocalDate.parse(date));
        }
        if (subject != null) {
            return taskRepository.findByUserIdAndSubjectIgnoreCase(userId, subject);
        }
        if (priority != null) {
            return taskRepository.findByUserIdAndPriority(userId, priority);
        }
        return taskRepository.findByUserIdOrderByDateAsc(userId);
    }

    public StudyTask updateTask(String userId, String taskId, TaskRequest request) {
        StudyTask task = getTaskByIdAndUser(userId, taskId);
        applyRequest(task, request);
        return taskRepository.save(task);
    }

    public StudyTask toggleComplete(String userId, String taskId, boolean completed) {
        StudyTask task = getTaskByIdAndUser(userId, taskId);
        task.setStatus(completed ? TaskStatus.COMPLETED : TaskStatus.PENDING);
        return taskRepository.save(task);
    }

    public void deleteTask(String userId, String taskId) {
        StudyTask task = getTaskByIdAndUser(userId, taskId);
        taskRepository.delete(task);
    }

    public StudyTask getOwnedTask(String userId, String taskId) {
        return getTaskByIdAndUser(userId, taskId);
    }

    private StudyTask getTaskByIdAndUser(String userId, String taskId) {
        StudyTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException("Task not found"));
        if (!task.getUserId().equals(userId)) {
            throw new ApiException("You are not allowed to access this task");
        }
        return task;
    }

    private void applyRequest(StudyTask task, TaskRequest request) {
        task.setTitle(request.title());
        task.setSubject(request.subject());
        task.setDate(request.date());
        task.setDuration(request.duration());
        task.setPriority(request.priority());
        task.setStatus(request.status());
    }
}
