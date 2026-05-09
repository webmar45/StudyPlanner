package com.smartstudyplanner.backend.dto.learning;

public record YoutubeVideoItem(
        String videoId,
        String title,
        String channelTitle,
        String thumbnailUrl,
        String watchUrl
) {}
