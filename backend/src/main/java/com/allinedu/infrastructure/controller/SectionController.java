package com.allinedu.infrastructure.controller;

import com.allinedu.application.dto.SectionResponse;
import com.allinedu.application.dto.StudentResponse;
import com.allinedu.application.service.SectionService;
import com.allinedu.domain.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sections")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping("/me")
    public List<SectionResponse> mySections(@AuthenticationPrincipal User user) {
        return sectionService.getMySections(user);
    }

    @GetMapping("/{sectionId}/students")
    public List<StudentResponse> students(@AuthenticationPrincipal User user,
                                          @PathVariable Long sectionId) {
        return sectionService.getSectionStudents(user, sectionId);
    }
}