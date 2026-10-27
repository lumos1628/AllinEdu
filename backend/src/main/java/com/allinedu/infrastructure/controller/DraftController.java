package com.allinedu.infrastructure.controller;

import com.allinedu.application.dto.SessionDetailResponse;
import com.allinedu.application.service.DraftService;
import com.allinedu.domain.model.User;
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
