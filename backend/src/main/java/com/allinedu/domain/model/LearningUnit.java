package com.allinedu.domain.model;

import java.time.LocalDate;

/**
 * Una unidad didáctica de la sección, con el avance de sus sesiones.
 * Las sesiones publicadas son las que el docente ya aprobó (Regla 0a).
 */
public record LearningUnit(
        Long id,
        String title,
        String situation,
        LocalDate startDate,
        LocalDate endDate,
        int approvedSessions,
        int totalSessions) {
}
