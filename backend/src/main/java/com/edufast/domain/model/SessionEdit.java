package com.edufast.domain.model;

import com.edufast.domain.exception.BusinessRuleException;

import java.util.List;
import java.util.Set;

/**
 * Lo que el docente cambia de una sesión. Las actividades llegan completas y en
 * orden: las que traen id ya existían, las que no son nuevas.
 */
public record SessionEdit(String title, String purpose, Integer durationMinutes, String evidence,
                          List<ActivityEdit> activities) {

    public static final Set<String> MOMENTS = Set.of("INICIO", "DESARROLLO", "CIERRE", "OTRO");

    public record ActivityEdit(Long id, String moment, String title, String instructions, Integer minutes) {
    }

    public void validate() {
        if (title == null || title.isBlank()) {
            throw new BusinessRuleException("La sesión necesita un título.");
        }
        if (durationMinutes != null && durationMinutes <= 0) {
            throw new BusinessRuleException("La duración debe ser mayor que cero.");
        }
        for (ActivityEdit a : activities) {
            if (a.moment() == null || !MOMENTS.contains(a.moment())) {
                throw new BusinessRuleException("Cada actividad va en inicio, desarrollo o cierre.");
            }
            if (a.instructions() == null || a.instructions().isBlank()) {
                throw new BusinessRuleException("Cada actividad necesita su descripción.");
            }
            if (a.minutes() != null && a.minutes() <= 0) {
                throw new BusinessRuleException("Los minutos de una actividad deben ser mayores que cero.");
            }
        }
    }
}
