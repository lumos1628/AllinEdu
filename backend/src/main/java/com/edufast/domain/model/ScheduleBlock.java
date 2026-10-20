package com.edufast.domain.model;

import java.time.LocalTime;

/**
 * Un bloque del horario semanal de una sección: día, horas y área.
 * taughtByTeacher indica si lo dicta el docente que consulta o un especialista.
 */
public record ScheduleBlock(int dayOfWeek, LocalTime start, LocalTime end, String area, boolean taughtByTeacher) {
}
