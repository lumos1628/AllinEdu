package com.allinedu.infrastructure.controller;

import com.allinedu.application.dto.ClosureResponse;
import com.allinedu.application.dto.MarkRequest;
import com.allinedu.application.service.ClosureService;
import com.allinedu.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sections/{sectionId}/closure")
public class ClosureController {

    private final ClosureService closureService;

    public ClosureController(ClosureService closureService) {
        this.closureService = closureService;
    }

    @GetMapping
    public ClosureResponse closure(@AuthenticationPrincipal User user, @PathVariable Long sectionId) {
        return closureService.getClosure(user, sectionId);
    }

    @PutMapping("/marks")
    public ClosureResponse saveMark(@AuthenticationPrincipal User user, @PathVariable Long sectionId,
                                    @Valid @RequestBody MarkRequest request) {
        return closureService.saveMark(user, sectionId, request);
    }
}
