package com.edufast.infrastructure.controller;

import com.edufast.application.dto.SessionDetailResponse;
import com.edufast.application.service.DraftService;
import com.edufast.domain.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/draft")
public class DraftController {

    private final DraftService draftService;

    public DraftController(DraftService draftService) {
        this.draftService = draftService;
    }

    @PostMapping
    public SessionDetailResponse generate(@AuthenticationPrincipal User user, @PathVariable Long sessionId) {
        return draftService.generateDraft(user, sessionId);
    }
}
