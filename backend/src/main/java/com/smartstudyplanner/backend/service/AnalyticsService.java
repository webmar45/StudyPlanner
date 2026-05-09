package com.smartstudyplanner.backend.service;

import com.smartstudyplanner.backend.dto.analytics.AnalyticsResponse;
import com.smartstudyplanner.backend.model.StudySession;
import com.smartstudyplanner.backend.model.enums.TaskStatus;
import com.smartstudyplanner.backend.repository.StudyTaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    private final FocusSessionService focusSessionService;
    private final StudyTaskRepository taskRepository;

    public AnalyticsService(FocusSessionService focusSessionService, StudyTaskRepository taskRepository) {
        this.focusSessionService = focusSessionService;
        this.taskRepository = taskRepository;
    }

    public AnalyticsResponse getAnalytics(String userId) {
        return getAnalytics(userId, null);
    }

    public AnalyticsResponse getAnalytics(String userId, LocalDate forDate) {
        LocalDate day = forDate != null ? forDate : LocalDate.now();
        LocalDateTime startOfDay = day.atStartOfDay();
        LocalDateTime endOfDay = day.plusDays(1).atStartOfDay().minusSeconds(1);
        LocalDateTime startOfWeek = day.minusDays(6).atStartOfDay();

        List<StudySession> daySessions = focusSessionService.getSessionsForRange(userId, startOfDay, endOfDay);
        int dayStudyMinutes = daySessions.stream()
                .mapToInt(s -> s.getDuration() == null ? 0 : s.getDuration())
                .sum();

        List<StudySession> weekSessions = focusSessionService.getSessionsForRange(userId, startOfWeek, endOfDay);
        int weekStudyMinutes = weekSessions.stream()
                .mapToInt(s -> s.getDuration() == null ? 0 : s.getDuration())
                .sum();

        long completedTasks = taskRepository.countByUserIdAndStatus(userId, TaskStatus.COMPLETED);
        long pendingTasks = taskRepository.countByUserIdAndStatus(userId, TaskStatus.PENDING);

        Map<LocalDate, Integer> dailySum = weekSessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getStartTime().toLocalDate(),
                        Collectors.summingInt(s -> s.getDuration() == null ? 0 : s.getDuration())
                ));

        List<AnalyticsResponse.DailyPoint> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = day.minusDays(i);
            trend.add(new AnalyticsResponse.DailyPoint(
                    d.getDayOfWeek().name().substring(0, 3),
                    dailySum.getOrDefault(d, 0)
            ));
        }

        List<AnalyticsResponse.UnifiedFocusRow> focusBreakdown = buildUnifiedFocusBreakdown(daySessions);

        return new AnalyticsResponse(
                dayStudyMinutes,
                weekStudyMinutes,
                completedTasks,
                pendingTasks,
                trend,
                focusBreakdown
        );
    }

    private List<AnalyticsResponse.UnifiedFocusRow> buildUnifiedFocusBreakdown(List<StudySession> daySessions) {
        List<AnalyticsResponse.UnifiedFocusRow> rows = new ArrayList<>();

        Map<String, Integer> minutesByTask = daySessions.stream()
                .filter(s -> s.getTaskId() != null && !s.getTaskId().isBlank())
                .collect(Collectors.groupingBy(
                        StudySession::getTaskId,
                        Collectors.summingInt(s -> s.getDuration() == null ? 0 : s.getDuration())
                ));

        for (Map.Entry<String, Integer> entry : minutesByTask.entrySet()) {
            String taskId = entry.getKey();
            String title = taskRepository.findById(taskId)
                    .map(t -> t.getTitle() + " (" + t.getSubject() + ")")
                    .orElse("Unknown task");
            String subtitle = taskActivitySubtitle(daySessions, taskId, title);
            rows.add(new AnalyticsResponse.UnifiedFocusRow(
                    "task:" + taskId,
                    title,
                    subtitle,
                    entry.getValue(),
                    "TASK"
            ));
        }

        Map<String, Integer> unlinkedByActivity = daySessions.stream()
                .filter(s -> s.getTaskId() == null || s.getTaskId().isBlank())
                .collect(Collectors.groupingBy(
                        s -> (s.getActivityName() == null || s.getActivityName().isBlank())
                                ? "General Activity"
                                : s.getActivityName().trim(),
                        Collectors.summingInt(s -> s.getDuration() == null ? 0 : s.getDuration())
                ));

        for (Map.Entry<String, Integer> entry : unlinkedByActivity.entrySet()) {
            String activity = entry.getKey();
            rows.add(new AnalyticsResponse.UnifiedFocusRow(
                    "unlinked:" + activity,
                    activity,
                    "Not linked to a task",
                    entry.getValue(),
                    "UNLINKED"
            ));
        }

        rows.sort(Comparator.comparingInt(AnalyticsResponse.UnifiedFocusRow::minutes).reversed());
        return rows;
    }

    private String taskActivitySubtitle(List<StudySession> daySessions, String taskId, String taskTitle) {
        List<String> acts = daySessions.stream()
                .filter(s -> taskId.equals(s.getTaskId()))
                .map(s -> s.getActivityName() == null ? "" : s.getActivityName().trim())
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
        if (acts.isEmpty()) {
            return "";
        }
        String joined = String.join(" · ", acts);
        if (joined.equals(taskTitle)) {
            return "";
        }
        return joined;
    }
}
