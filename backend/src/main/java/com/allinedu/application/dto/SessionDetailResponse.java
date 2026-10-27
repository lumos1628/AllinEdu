package com.allinedu.application.dto;

import com.allinedu.domain.model.SessionDetail;

import java.time.Instant;
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
        boolean generated,
        Instant editedAt,
        Instant approvedAt,
        List<String> competencies,
        List<String> criteria,
        List<Evidence> evidences,
        Instrument instrument,
        List<Note> dua,
        List<Note> approaches,
        List<Material> materials,
        List<Activity> activities,
        boolean regenerable,
        List<Knot> knots) {

    public record Evidence(String description, String format) {
    }

    public record Instrument(String name, String type, List<Criterion> criteria) {
    }

    public record Criterion(String description, List<Level> levels) {
    }

    public record Level(String code, String description) {
    }

    public record Note(String title, String text, boolean generated) {
    }

    public record Material(String name, Integer quantity, boolean required, String description, boolean generated) {
    }

    /** Un nudo del borrador: state PENDIENTE, ACEPTADO o CAMBIO. */
    public record Knot(Long id, String type, String question, String fragment, String state, String answer) {
    }

    public record Activity(Long id, int order, String moment, String title, String instructions, Integer minutes,
                           boolean generated) {
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
                !s.isDraft(), s.generated(), s.editedAt(), s.approvedAt(), s.competencies(), s.criteria(),
                s.evidences().stream().map(e -> new Evidence(e.description(), e.format())).toList(),
                instrument,
                s.dua().stream().map(n -> new Note(n.title(), n.text(), n.generated())).toList(),
                s.approaches().stream().map(n -> new Note(n.title(), n.text(), n.generated())).toList(),
                s.materials().stream()
                        .map(m -> new Material(m.name(), m.quantity(), "OBLIGATORIO".equals(m.character()), m.description(),
                                m.generated()))
                        .toList(),
                s.activities().stream()
                        .map(a -> new Activity(a.id(), a.order(), a.moment(), a.title(), a.instructions(), a.minutes(),
                                a.generated()))
                        .toList(),
                !s.hasTeacherWriting(),
                s.knots().stream()
                        .map(k -> new Knot(k.id(), k.type(), k.question(), k.fragment(), k.state(), k.answer()))
                        .toList());
    }
}
