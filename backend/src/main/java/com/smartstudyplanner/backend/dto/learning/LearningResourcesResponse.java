package com.smartstudyplanner.backend.dto.learning;

import java.util.List;

public record LearningResourcesResponse(
        String queryUsed,
        boolean youtubeConfigured,
        List<YoutubeVideoItem> youtubeVideos,
        List<WikipediaArticleItem> wikipediaArticles
) {}
