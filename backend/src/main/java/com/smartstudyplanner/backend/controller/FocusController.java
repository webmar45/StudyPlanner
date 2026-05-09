package com.smartstudyplanner.backend.controller;

import com.smartstudyplanner.backend.dto.session.SessionActionResponse;
import com.smartstudyplanner.backend.dto.session.StartSessionRequest;
import com.smartstudyplanner.backend.model.StudySession;
import com.smartstudyplanner.backend.service.FocusSessionService;
import com.smartstudyplanner.backend.util.AuthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/session")
public class FocusController {
    private final FocusSessionService focusSessionService;
    private final AuthUtil authUtil;

    public FocusController(FocusSessionService focusSessionService, AuthUtil authUtil) {
        this.focusSessionService = focusSessionService;
        this.authUtil = authUtil;
    }

    @PostMapping("/start")
    public SessionActionResponse start(
            Authentication authentication,
            @RequestBody(required = false) StartSessionRequest request
    ) {
        String userId = authUtil.getUserId(authentication);
        String activityName = request == null || request.activityName() == null || request.activityName().isBlank()
                ? "General Activity"
                : request.activityName();
        String taskId = request == null ? null : request.taskId();
        StudySession session = focusSessionService.startSession(userId, activityName, taskId);
        return new SessionActionResponse("Study session started", session.getId(), session.getDuration());
    }

    @PostMapping("/end")
    public SessionActionResponse end(Authentication authentication) {
        String userId = authUtil.getUserId(authentication);
        StudySession session = focusSessionService.endSession(userId);
        return new SessionActionResponse("Study session ended", session.getId(), session.getDuration());
    }

    @GetMapping
    public List<StudySession> getSessions(
            Authentication authentication,
            @RequestParam String start,
            @RequestParam String end
    ) {
        String userId = authUtil.getUserId(authentication);
        return focusSessionService.getSessionsForRange(
                userId,
                LocalDateTime.parse(start),
                LocalDateTime.parse(end)
        );
    }
}
