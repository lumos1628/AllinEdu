package com.edufast.infrastructure.controller;

import com.edufast.application.dto.SessionDetailResponse;
import com.edufast.application.dto.UnitDetailResponse;
import com.edufast.application.dto.WeekResponse;
import com.edufast.application.service.PlanningService;
import com.edufast.domain.model.User;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
public class PlanningController {

    private final PlanningService planningService;

    public PlanningController(PlanningService planningService) {
        this.planningService = planningService;
    }

    @GetMapping("/sections/{sectionId}/week")
    public WeekResponse week(@AuthenticationPrincipal User user,
                             @PathVariable Long sectionId,
                             @RequestParam(required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return planningService.getWeek(user, sectionId, date);
    }

    @GetMapping("/units/{unitId}")
    public UnitDetailResponse unit(@AuthenticationPrincipal User user, @PathVariable Long unitId) {
        return planningService.getUnit(user, unitId);
    }

    @GetMapping("/sessions/{sessionId}")
    public SessionDetailResponse session(@AuthenticationPrincipal User user, @PathVariable Long sessionId) {
        return planningService.getSession(user, sessionId);
    }

    @PostMapping("/sessions/{sessionId}/approval")
    public SessionDetailResponse approve(@AuthenticationPrincipal User user, @PathVariable Long sessionId) {
        return planningService.approveSession(user, sessionId);
    }
}
