package com.allinedu.application.service;

import com.allinedu.application.dto.AttendanceRequest;
import com.allinedu.application.dto.AttendanceResponse;
import com.allinedu.domain.model.User;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    List<AttendanceResponse> takeAttendance(User user, Long sectionId, AttendanceRequest request);

    List<AttendanceResponse> getAttendance(User user, Long sectionId, LocalDate date);
}