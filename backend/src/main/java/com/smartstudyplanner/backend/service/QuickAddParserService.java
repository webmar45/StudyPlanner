package com.smartstudyplanner.backend.service;

import com.smartstudyplanner.backend.dto.llm.ParsedTaskDraftResponse;
import com.smartstudyplanner.backend.model.enums.Priority;
import com.smartstudyplanner.backend.model.enums.TaskStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QuickAddParserService {

    private static final Pattern DURATION = Pattern.compile("(\\d+)\\s*(min|minutes?|m)\\b", Pattern.CASE_INSENSITIVE);

    public ParsedTaskDraftResponse parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ParsedTaskDraftResponse("Study block", "General", LocalDate.now(), 60, Priority.MEDIUM, TaskStatus.PENDING);
        }
        String working = raw.trim();
        String lower = working.toLowerCase();

        Priority priority = Priority.MEDIUM;
        if (lower.contains("high priority") || lower.contains("priority high")) {
            priority = Priority.HIGH;
        } else if (lower.contains("low priority") || lower.contains("priority low")) {
            priority = Priority.LOW;
        }

        LocalDate date = LocalDate.now();
        if (lower.contains("tomorrow")) {
            date = date.plusDays(1);
        }

        int duration = 60;
        Matcher dm = DURATION.matcher(working);
        if (dm.find()) {
            duration = Math.max(5, Integer.parseInt(dm.group(1)));
            working = working.substring(0, dm.start()) + " " + working.substring(dm.end());
        }

        String cleaned = working
                .replaceAll("(?i)\\b(high priority|low priority|medium priority|priority high|priority low)\\b", "")
                .replaceAll("(?i)\\btoday\\b", "")
                .replaceAll("(?i)\\btomorrow\\b", "")
                .replaceAll("\\s+", " ")
                .trim();

        String title = cleaned.isEmpty() ? "Study block" : cleaned;
        String subject = "General";
        if (title.contains(" - ")) {
            String[] parts = title.split("\\s-\\s", 2);
            subject = parts[0].trim().isEmpty() ? "General" : parts[0].trim();
            title = parts[1].trim().isEmpty() ? "Study block" : parts[1].trim();
        }

        return new ParsedTaskDraftResponse(title, subject, date, duration, priority, TaskStatus.PENDING);
    }
}
