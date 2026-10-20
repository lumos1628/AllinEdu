package com.edufast.domain.model;

import java.time.LocalDate;
import java.util.List;

/**
 * El cierre del periodo abierto de una sección: estudiantes, competencias y
 * el nivel que el docente registró para cada par.
 */
public record BimesterClosure(Period period, List<Competency> competencies, List<StudentMarks> students) {

    public record Period(Long id, String name, LocalDate startDate, LocalDate endDate) {
    }

    public record Competency(Long id, String name) {
    }

    public record StudentMarks(Long enrollmentId, String name, String code, List<Mark> marks) {
    }

    public record Mark(Long competencyId, String level, String previousLevel, String conclusion, String recommendation) {
    }

    public boolean hasCompetency(Long competencyId) {
        return competencies.stream().anyMatch(c -> c.id().equals(competencyId));
    }

    public boolean hasStudent(Long enrollmentId) {
        return students.stream().anyMatch(s -> s.enrollmentId().equals(enrollmentId));
    }
}
