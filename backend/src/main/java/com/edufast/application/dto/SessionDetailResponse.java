package com.edufast.application.dto;

import com.edufast.domain.model.SessionDetail;

import java.util.List;

public record SessionDetailResponse(
        Long id,
        Long unitId,
        String unitTitle,
        int number,
        String title,
        String purpose,
        Integer durationMinutes,
        boolean approved,
        List<String> competencies,
        List<String> criteria,
        List<Evidence> evidences,
        Instrument instrument,
        List<Note> dua,
        List<Note> approaches,
        List<Material> materials,
        List<Activity> activities) {

    public record Evidence(String description, String format) {
    }

    public record Instrument(String name, String type, List<Criterion> criteria) {
    }

    public record Criterion(String description, List<Level> levels) {
    }

    public record Level(String code, String description) {
    }

    public record Note(String title, String text) {
    }

    public record Material(String name, Integer quantity, boolean required, String description) {
    }

    public record Activity(int order, String moment, String title, String instructions, Integer minutes) {
    }

    public static SessionDetailResponse from(SessionDetail s) {
        var instrument = s.instrument() == null ? null : new Instrument(
                s.instrument().name(), s.instrument().type(),
                s.instrument().criteria().stream()
                        .map(c -> new Criterion(c.description(),
                                c.levels().stream().map(l -> new Level(l.code(), l.description())).toList()))
                        .toList());
        return new SessionDetailResponse(
                s.id(), s.unitId(), s.unitTitle(), s.number(), s.title(), s.purpose(), s.durationMinutes(),
                !s.isDraft(), s.competencies(), s.criteria(),
                s.evidences().stream().map(e -> new Evidence(e.description(), e.format())).toList(),
                instrument,
                s.dua().stream().map(n -> new Note(n.title(), n.text())).toList(),
                s.approaches().stream().map(n -> new Note(n.title(), n.text())).toList(),
                s.materials().stream()
                        .map(m -> new Material(m.name(), m.quantity(), "OBLIGATORIO".equals(m.character()), m.description()))
                        .toList(),
                s.activities().stream()
                        .map(a -> new Activity(a.order(), a.moment(), a.title(), a.instructions(), a.minutes()))
                        .toList());
    }
}
