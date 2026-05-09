package com.smartstudyplanner.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartstudyplanner.backend.dto.analytics.AnalyticsResponse;
import com.smartstudyplanner.backend.dto.llm.WeeklyInsightResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;

@Service
public class LlmInsightService {

    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    @Value("${app.openai.api-key:}")
    private String openaiApiKey;

    @Value("${app.openai.model:gpt-4o-mini}")
    private String openaiModel;

    public LlmInsightService(AnalyticsService analyticsService, ObjectMapper objectMapper) {
        this.analyticsService = analyticsService;
        this.objectMapper = objectMapper;
    }

    public WeeklyInsightResponse weeklySummary(String userId) {
        AnalyticsResponse a = analyticsService.getAnalytics(userId, LocalDate.now());
        String facts = buildFacts(a);
        if (openaiApiKey != null && !openaiApiKey.isBlank()) {
            try {
                String content = callOpenAi(facts);
                if (content != null && !content.isBlank()) {
                    return new WeeklyInsightResponse(content.trim(), "openai");
                }
            } catch (Exception ignored) {
                // fall through to template
            }
        }
        return new WeeklyInsightResponse(templateFromAnalytics(a), "template");
    }

    private String buildFacts(AnalyticsResponse a) {
        StringBuilder sb = new StringBuilder();
        sb.append("- Focus minutes today: ").append(a.todayStudyMinutes()).append('\n');
        sb.append("- Focus minutes last 7 days: ").append(a.weekStudyMinutes()).append('\n');
        sb.append("- Completed tasks (all time in app): ").append(a.completedTasks()).append('\n');
        sb.append("- Pending tasks: ").append(a.pendingTasks()).append('\n');
        if (a.focusBreakdown() != null && !a.focusBreakdown().isEmpty()) {
            sb.append("- Focus breakdown today (tasks + unlinked activities):\n");
            a.focusBreakdown().stream().limit(8).forEach(p ->
                    sb.append("  - ").append(p.title()).append(" [").append(p.segment()).append("]: ")
                            .append(p.minutes()).append(" min\n"));
        }
        return sb.toString();
    }

    private String templateFromAnalytics(AnalyticsResponse a) {
        StringBuilder sb = new StringBuilder();
        sb.append("Weekly snapshot:\n");
        sb.append("• You logged ").append(a.weekStudyMinutes()).append(" focused minutes in the last 7 days.\n");
        sb.append("• Today: ").append(a.todayStudyMinutes()).append(" minutes.\n");
        sb.append("• Pending tasks: ").append(a.pendingTasks()).append("; completed: ").append(a.completedTasks()).append(".\n");
        if (a.focusBreakdown() != null && !a.focusBreakdown().isEmpty()) {
            AnalyticsResponse.UnifiedFocusRow top = a.focusBreakdown().get(0);
            sb.append("• Most time today: ").append(top.title()).append(" (").append(top.minutes()).append(" min).\n");
        }
        sb.append("Keep a consistent daily block to build momentum.");
        return sb.toString();
    }

    private String callOpenAi(String facts) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", openaiModel);
        ArrayNode messages = root.putArray("messages");
        ObjectNode msg = messages.addObject();
        msg.put("role", "user");
        msg.put("content", "You are a supportive study coach. Reply with 3-5 short bullet points in plain text. "
                + "Use the following user stats only:\n\n" + facts);

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + openaiApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(root)))
                .timeout(Duration.ofSeconds(45))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) {
            throw new IllegalStateException("OpenAI error: " + resp.statusCode());
        }
        JsonNode tree = objectMapper.readTree(resp.body());
        return tree.path("choices").path(0).path("message").path("content").asText("");
    }
}
