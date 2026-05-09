package com.smartstudyplanner.backend.repository;

import com.smartstudyplanner.backend.model.StudyTask;
import com.smartstudyplanner.backend.model.enums.Priority;
import com.smartstudyplanner.backend.model.enums.TaskStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface StudyTaskRepository extends MongoRepository<StudyTask, String> {
    List<StudyTask> findByUserIdOrderByDateAsc(String userId);
    List<StudyTask> findByUserIdAndDate(String userId, LocalDate date);
    List<StudyTask> findByUserIdAndSubjectIgnoreCase(String userId, String subject);
    List<StudyTask> findByUserIdAndPriority(String userId, Priority priority);
    long countByUserIdAndStatus(String userId, TaskStatus status);
}
