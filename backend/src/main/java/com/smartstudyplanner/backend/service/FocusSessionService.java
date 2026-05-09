package com.smartstudyplanner.backend.service;

import com.smartstudyplanner.backend.exception.ApiException;
import com.smartstudyplanner.backend.model.StudySession;
import com.smartstudyplanner.backend.model.StudyTask;
import com.smartstudyplanner.backend.repository.StudySessionRepository;
import com.smartstudyplanner.backend.repository.StudyTaskRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FocusSessionService {
    private final StudySessionRepository studySessionRepository;
    private final StudyTaskRepository studyTaskRepository;

    public FocusSessionService(StudySessionRepository studySessionRepository, StudyTaskRepository studyTaskRepository) {
        this.studySessionRepository = studySessionRepository;
        this.studyTaskRepository = studyTaskRepository;
    }

    public StudySession startSession(String userId) {
        return startSession(userId, "General Activity", null);
    }

    public StudySession startSession(String userId, String activityName, String taskId) {
        studySessionRepository.findFirstByUserIdAndEndTimeIsNullOrderByStartTimeDesc(userId).ifPresent(active -> {
            throw new ApiException("An active study session already exists");
        });

        String normalizedTaskId = taskId == null || taskId.isBlank() ? null : taskId.trim();
        if (normalizedTaskId != null) {
            StudyTask task = studyTaskRepository.findById(normalizedTaskId)
                    .orElseThrow(() -> new ApiException("Task not found"));
            if (!task.getUserId().equals(userId)) {
                throw new ApiException("You are not allowed to link this task");
            }
        }

        StudySession session = new StudySession();
        session.setUserId(userId);
        session.setActivityName(
                activityName == null || activityName.isBlank()
                        ? "General Activity"
                        : activityName.trim()
        );
        session.setTaskId(normalizedTaskId);
        session.setStartTime(LocalDateTime.now());
        return studySessionRepository.save(session);
    }

    public StudySession endSession(String userId) {
        StudySession session = studySessionRepository.findFirstByUserIdAndEndTimeIsNullOrderByStartTimeDesc(userId)
                .orElseThrow(() -> new ApiException("No active study session found"));

        LocalDateTime now = LocalDateTime.now();
        session.setEndTime(now);
        session.setDuration((int) Duration.between(session.getStartTime(), now).toMinutes());
        return studySessionRepository.save(session);
    }

    public List<StudySession> getSessionsForRange(String userId, LocalDateTime start, LocalDateTime end) {
        return studySessionRepository.findByUserIdAndEndTimeIsNotNullAndStartTimeBetweenOrderByStartTimeAsc(userId, start, end);
    }
}
