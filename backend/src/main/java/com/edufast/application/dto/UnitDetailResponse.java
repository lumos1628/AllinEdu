package com.edufast.application.dto;

import com.edufast.domain.model.UnitDetail;

import java.time.LocalDate;
import java.util.List;

public record UnitDetailResponse(
        Long id,
        String title,
        String situation,
        String purpose,
        LocalDate startDate,
        LocalDate endDate,
        List<Competency> competencies,
        List<Session> sessions) {

    public record Competency(Long id, String name, String purpose, int sessions) {
    }

    public record Session(Long id, int number, String title, String purpose, boolean approved,
                          List<String> competencies, int activities) {
    }

    public static UnitDetailResponse from(UnitDetail u) {
        var sessions = u.sessions().stream()
                .map(s -> new Session(s.id(), s.number(), s.title(), s.purpose(),
                        "PUBLICADA".equals(s.status()), s.competencies(), s.activities()))
                .toList();
        var competencies = u.competencies().stream()
                .map(c -> new Competency(c.id(), c.name(), c.purpose(),
                        (int) u.sessions().stream().filter(s -> s.competencies().contains(c.name())).count()))
                .toList();
        return new UnitDetailResponse(u.id(), u.title(), u.situation(), u.purpose(),
                u.startDate(), u.endDate(), competencies, sessions);
    }
}
