package com.allinedu.domain.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Un bloque del horario de una sección, visto por un docente concreto.
 */
public record Slot(Long id, Long sectionId, int dayOfWeek, LocalTime start, LocalTime end,
                   Long areaId, String area, boolean taughtByTeacher) {

    /** El bloque solo se dicta en su día de la semana (1 = lunes). */
    public boolean fallsOn(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.of(dayOfWeek);
    }

    public int minutes() {
        return (int) java.time.Duration.between(start, end).toMinutes();
    }
}
