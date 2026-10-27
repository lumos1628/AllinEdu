package com.allinedu.infrastructure.controller;

import com.allinedu.application.dto.AttendanceRequest;
import com.allinedu.application.dto.AttendanceResponse;
import com.allinedu.application.service.AttendanceService;
import com.allinedu.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sections/{sectionId}/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    public List<AttendanceResponse> save(@AuthenticationPrincipal User user,
                                         @PathVariable Long sectionId,
                                         @Valid @RequestBody AttendanceRequest request) {
        return attendanceService.takeAttendance(user, sectionId, request);
    }

    @GetMapping
    public List<AttendanceResponse> get(@AuthenticationPrincipal User user,
                                        @PathVariable Long sectionId,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attendanceService.getAttendance(user, sectionId, date);
    }
}