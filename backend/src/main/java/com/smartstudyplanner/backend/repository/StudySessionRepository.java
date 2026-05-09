package com.smartstudyplanner.backend.repository;

import com.smartstudyplanner.backend.model.StudySession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudySessionRepository extends MongoRepository<StudySession, String> {
    Optional<StudySession> findFirstByUserIdAndEndTimeIsNullOrderByStartTimeDesc(String userId);
    List<StudySession> findByUserIdAndEndTimeIsNotNullAndStartTimeBetweenOrderByStartTimeAsc(
            String userId,
            LocalDateTime start,
            LocalDateTime end
    );
}
