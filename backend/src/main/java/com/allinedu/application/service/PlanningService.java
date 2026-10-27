package com.allinedu.application.service;

import com.allinedu.application.dto.SessionDetailResponse;
import com.allinedu.application.dto.SessionUpdateRequest;
import com.allinedu.application.dto.UnitDetailResponse;
import com.allinedu.application.dto.WeekResponse;
import com.allinedu.domain.model.User;

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

    /** Guarda lo que el docente escribió. Una sesión aprobada sigue aprobada: el texto es suyo. */
    SessionDetailResponse updateSession(User user, Long sessionId, SessionUpdateRequest request);
}
