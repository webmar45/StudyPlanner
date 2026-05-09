package com.smartstudyplanner.backend.controller;

import com.smartstudyplanner.backend.dto.llm.ParsedTaskDraftResponse;
import com.smartstudyplanner.backend.dto.llm.QuickAddRequest;
import com.smartstudyplanner.backend.dto.llm.WeeklyInsightResponse;
import com.smartstudyplanner.backend.service.LlmInsightService;
import com.smartstudyplanner.backend.service.QuickAddParserService;
import com.smartstudyplanner.backend.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/llm")
public class LlmController {

    private final LlmInsightService llmInsightService;
    private final QuickAddParserService quickAddParserService;
    private final AuthUtil authUtil;

    public LlmController(
            LlmInsightService llmInsightService,
            QuickAddParserService quickAddParserService,
            AuthUtil authUtil
    ) {
        this.llmInsightService = llmInsightService;
        this.quickAddParserService = quickAddParserService;
        this.authUtil = authUtil;
    }

    @GetMapping("/weekly-summary")
    public WeeklyInsightResponse weeklySummary(Authentication authentication) {
        String userId = authUtil.getUserId(authentication);
        return llmInsightService.weeklySummary(userId);
    }

    @PostMapping("/quick-add")
    public ParsedTaskDraftResponse quickAdd(
            Authentication authentication,
            @Valid @RequestBody QuickAddRequest request
    ) {
        authUtil.getUserId(authentication);
        return quickAddParserService.parse(request.text());
    }
}
