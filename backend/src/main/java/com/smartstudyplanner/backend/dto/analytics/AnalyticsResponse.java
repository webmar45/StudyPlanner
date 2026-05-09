package com.smartstudyplanner.backend.dto.analytics;

import java.util.List;

public record AnalyticsResponse(
        int todayStudyMinutes,
        int weekStudyMinutes,
        long completedTasks,
        long pendingTasks,
        List<DailyPoint> productivityTrend,
        /** Task-linked minutes plus unlinked minutes grouped by activity — single non-overlapping view */
        List<UnifiedFocusRow> focusBreakdown
) {
    public record DailyPoint(String day, int minutes) {}

    /**
     * @param rowKey   stable id for UI (e.g. task:abc or unlinked:General Activity)
     * @param title    primary label (task title or activity name)
     * @param subtitle optional detail (e.g. session activity label); may be blank
     * @param minutes  focus minutes for this row
     * @param segment  TASK or UNLINKED
     */
    public record UnifiedFocusRow(
            String rowKey,
            String title,
            String subtitle,
            int minutes,
            String segment
    ) {}
}
