package com.allinedu.infrastructure.controller;

import com.allinedu.application.dto.ClassroomRequest;
import com.allinedu.application.dto.ClassroomResponse;
import com.allinedu.application.dto.DraftJobResponse;
import com.allinedu.application.dto.DraftRequest;
import com.allinedu.application.dto.KnotAnswerRequest;
import com.allinedu.application.dto.SessionDetailResponse;
import com.allinedu.application.service.DraftService;
import com.allinedu.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** El telar: borradores de sesión, preparación de la unidad, nudos y "Mi aula". */
@RestController
@RequestMapping("/api/v1")
public class DraftController {

    private final DraftService draftService;

    public DraftController(DraftService draftService) {
        this.draftService = draftService;
    }

    @PostMapping("/sessions/{sessionId}/draft")
    public SessionDetailResponse generate(@AuthenticationPrincipal User user, @PathVariable Long sessionId,
                                          @Valid @RequestBody(required = false) DraftRequest request) {
        return draftService.generateDraft(user, sessionId, request);
    }

    @PutMapping("/sessions/{sessionId}/knots/{knotId}")
    public SessionDetailResponse answerKnot(@AuthenticationPrincipal User user, @PathVariable Long sessionId,
                                            @PathVariable Long knotId, @Valid @RequestBody KnotAnswerRequest request) {
        return draftService.answerKnot(user, sessionId, knotId, request);
    }

    @PostMapping("/units/{unitId}/drafts")
    public DraftJobResponse prepareUnit(@AuthenticationPrincipal User user, @PathVariable Long unitId) {
        return draftService.prepareUnit(user, unitId);
    }

    @GetMapping("/units/{unitId}/drafts")
    public DraftJobResponse unitJob(@AuthenticationPrincipal User user, @PathVariable Long unitId) {
        return draftService.getUnitJob(user, unitId);
    }

    @GetMapping("/me/classroom")
    public ClassroomResponse classroom(@AuthenticationPrincipal User user) {
        return draftService.getClassroom(user);
    }

    @PutMapping("/me/classroom")
    public ClassroomResponse saveClassroom(@AuthenticationPrincipal User user,
                                           @Valid @RequestBody ClassroomRequest request) {
        return draftService.saveClassroom(user, request);
    }
}
