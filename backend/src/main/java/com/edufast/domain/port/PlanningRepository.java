package com.edufast.domain.port;

import com.edufast.domain.model.LearningUnit;
import com.edufast.domain.model.ScheduleBlock;
import com.edufast.domain.model.SessionDetail;
import com.edufast.domain.model.UnitDetail;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * PUERTO: lo que el negocio necesita leer y cambiar de la planificación.
 */
public interface PlanningRepository {

    /** Bloques del horario vigente en la fecha indicada, ordenados por día y hora. */
    List<ScheduleBlock> findSchedule(Long sectionId, Long teacherId, LocalDate date);

    /** La unidad que transcurre en la fecha; si no hay, la próxima que empieza. */
    Optional<LearningUnit> findCurrentOrNextUnit(Long sectionId, LocalDate date);

    /** true si la unidad está dirigida a alguna sección asignada al docente. */
    boolean isUnitAssignedToTeacher(Long unitId, Long teacherId);

    Optional<UnitDetail> findUnit(Long unitId);

    Optional<SessionDetail> findSession(Long sessionId);

    /** Marca la sesión como aprobada por el docente. */
    void approveSession(Long sessionId);
}
