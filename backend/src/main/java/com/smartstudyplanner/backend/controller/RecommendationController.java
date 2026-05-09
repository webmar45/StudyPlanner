package com.smartstudyplanner.backend.controller;

import com.smartstudyplanner.backend.dto.recommendation.RecommendedTaskResponse;
import com.smartstudyplanner.backend.service.RecommendationService;
import com.smartstudyplanner.backend.util.AuthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final AuthUtil authUtil;

    public RecommendationController(RecommendationService recommendationService, AuthUtil authUtil) {
        this.recommendationService = recommendationService;
        this.authUtil = authUtil;
    }

    @GetMapping("/next-tasks")
    public List<RecommendedTaskResponse> nextTasks(
            Authentication authentication,
            @RequestParam(defaultValue = "5") int limit
    ) {
        String userId = authUtil.getUserId(authentication);
        int capped = Math.min(Math.max(limit, 1), 20);
        return recommendationService.getNextTasks(userId, capped);
    }
}
