package com.smartstudyplanner.backend.service;

import com.smartstudyplanner.backend.dto.recommendation.RecommendedTaskResponse;
import com.smartstudyplanner.backend.model.StudyTask;
import com.smartstudyplanner.backend.model.enums.Priority;
import com.smartstudyplanner.backend.model.enums.TaskStatus;
import com.smartstudyplanner.backend.repository.StudyTaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RecommendationService {

    private final StudyTaskRepository taskRepository;

    public RecommendationService(StudyTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<RecommendedTaskResponse> getNextTasks(String userId, int limit) {
        LocalDate today = LocalDate.now();
        List<StudyTask> pending = taskRepository.findByUserIdOrderByDateAsc(userId).stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING)
                .toList();

        List<StudyTask> sorted = new ArrayList<>(pending);
        sorted.sort(Comparator
                .comparingInt((StudyTask t) -> scheduleBucket(t, today))
                .thenComparingInt(this::priorityRank)
                .thenComparing(StudyTask::getDate)
                .thenComparing(StudyTask::getTitle, String.CASE_INSENSITIVE_ORDER));

        List<RecommendedTaskResponse> out = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, sorted.size()); i++) {
            StudyTask t = sorted.get(i);
            out.add(new RecommendedTaskResponse(
                    t.getId(),
                    t.getTitle(),
                    t.getSubject(),
                    t.getDate(),
                    t.getPriority().name(),
                    buildReason(t, today)
            ));
        }
        return out;
    }

    private int scheduleBucket(StudyTask t, LocalDate today) {
        if (t.getDate().equals(today)) {
            return 0;
        }
        if (t.getDate().isBefore(today)) {
            return 1;
        }
        return 2;
    }

    private int priorityRank(StudyTask t) {
        return switch (t.getPriority()) {
            case HIGH -> 0;
            case MEDIUM -> 1;
            case LOW -> 2;
        };
    }

    private String buildReason(StudyTask t, LocalDate today) {
        List<String> parts = new ArrayList<>();
        if (t.getDate().equals(today)) {
            parts.add("Scheduled for today");
        } else if (t.getDate().isBefore(today)) {
            parts.add("Overdue");
        } else {
            parts.add("Upcoming");
        }
        if (t.getPriority() == Priority.HIGH) {
            parts.add("High priority");
        }
        return String.join(" · ", parts);
    }
}
