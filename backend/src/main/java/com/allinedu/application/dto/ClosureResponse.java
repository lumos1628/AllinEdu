package com.allinedu.application.dto;

import com.allinedu.domain.model.BimesterClosure;

import java.time.LocalDate;
import java.util.List;

public record ClosureResponse(
        Long periodId,
        String periodName,
        LocalDate startDate,
        LocalDate endDate,
        List<Competency> competencies,
        List<Student> students,
        int pendingConclusions) {

    public record Competency(Long id, String name) {
    }

    public record Student(Long enrollmentId, String name, String code, List<Mark> marks) {
    }

    /** missingConclusion: el nivel exige conclusión y todavía no la tiene. */
    public record Mark(Long competencyId, String level, String previousLevel, String conclusion,
                       String recommendation, boolean missingConclusion) {
    }

    public static ClosureResponse from(BimesterClosure c) {
        var students = c.students().stream()
                .map(s -> new Student(s.enrollmentId(), s.name(), s.code(), s.marks().stream()
                        .map(m -> new Mark(m.competencyId(), m.level(), m.previousLevel(), m.conclusion(),
                                m.recommendation(), missing(m)))
                        .toList()))
                .toList();
        int pending = (int) students.stream().flatMap(s -> s.marks().stream()).filter(Mark::missingConclusion).count();
        return new ClosureResponse(c.period().id(), c.period().name(), c.period().startDate(), c.period().endDate(),
                c.competencies().stream().map(x -> new Competency(x.id(), x.name())).toList(), students, pending);
    }

    private static boolean missing(BimesterClosure.Mark m) {
        boolean requires = "B".equals(m.level()) || "C".equals(m.level());
        return requires && (isBlank(m.conclusion()) || isBlank(m.recommendation()));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
