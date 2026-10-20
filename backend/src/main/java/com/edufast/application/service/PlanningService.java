package com.edufast.application.service;

import com.edufast.application.dto.SessionDetailResponse;
import com.edufast.application.dto.UnitDetailResponse;
import com.edufast.application.dto.WeekResponse;
import com.edufast.domain.model.User;

import java.time.LocalDate;

public interface PlanningService {

    /**
     * La semana (lunes a domingo) que contiene la fecha: horario de la sección
     * y unidad en curso. Solo para secciones asignadas al docente.
     */
    WeekResponse getWeek(User user, Long sectionId, LocalDate date);

    UnitDetailResponse getUnit(User user, Long unitId);

    SessionDetailResponse getSession(User user, Long sessionId);

    /** El docente aprueba el borrador: desde aquí puede llegar a clase o imprimirse. */
    SessionDetailResponse approveSession(User user, Long sessionId);
}
