package com.edufast.infrastructure.controller;

import com.edufast.application.dto.PrepareSlotRequest;
import com.edufast.application.dto.SessionDetailResponse;
import com.edufast.application.dto.SlotOptionsResponse;
import com.edufast.application.service.SlotService;
import com.edufast.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/sections/{sectionId}/slots/{slotId}")
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @GetMapping
    public SlotOptionsResponse options(@AuthenticationPrincipal User user, @PathVariable Long sectionId,
                                       @PathVariable Long slotId,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return slotService.getOptions(user, sectionId, slotId, date);
    }

    @PostMapping("/session")
    public SessionDetailResponse prepare(@AuthenticationPrincipal User user, @PathVariable Long sectionId,
                                         @PathVariable Long slotId, @Valid @RequestBody PrepareSlotRequest request) {
        return slotService.prepare(user, sectionId, slotId, request);
    }
}
