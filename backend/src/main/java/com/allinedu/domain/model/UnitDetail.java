package com.allinedu.domain.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Una unidad didáctica con sus competencias y sus sesiones, en orden.
 */
public record UnitDetail(
        Long id,
        String title,
        String situation,
        String purpose,
        LocalDate startDate,
        LocalDate endDate,
        List<Competency> competencies,
        List<SessionSummary> sessions) {

    public record Competency(Long id, String name, String purpose) {
    }

    /** openKnots: preguntas del último borrador que el docente todavía no respondió. */
    public record SessionSummary(Long id, int number, String title, String purpose, String status,
                                 List<String> competencies, int activities, int openKnots) {
    }
}
