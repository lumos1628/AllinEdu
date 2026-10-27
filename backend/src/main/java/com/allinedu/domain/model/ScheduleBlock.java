package com.allinedu.domain.model;

import java.time.LocalTime;

/**
 * Un bloque del horario semanal de una sección: día, horas y área.
 * taughtByTeacher indica si lo dicta el docente que consulta o un especialista.
 * session es la sesión asignada a este bloque en esa semana, si la hay.
 */
public record ScheduleBlock(Long slotId, int dayOfWeek, LocalTime start, LocalTime end, String area,
                            boolean taughtByTeacher, ScheduledSession session) {

    public record ScheduledSession(Long id, Long unitId, String title, String status, int activities) {
    }
}
