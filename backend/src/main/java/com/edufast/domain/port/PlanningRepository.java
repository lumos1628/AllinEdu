package com.edufast.domain.port;

import com.edufast.domain.model.DraftContext;
import com.edufast.domain.model.GeneratedDraft;
import com.edufast.domain.model.LearningUnit;
import com.edufast.domain.model.ScheduleBlock;
import com.edufast.domain.model.SessionDetail;
import com.edufast.domain.model.SessionEdit;
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

    /** Solo la unidad de la sesión: basta para verificar el acceso sin cargarla entera. */
    Optional<Long> findSessionUnitId(Long sessionId);

    /** Marca la sesión como aprobada por el docente y guarda quién y cuándo. */
    void approveSession(Long sessionId, Long teacherId);

    /** Guarda los cambios del docente; lo que él escribe queda con origen DOCENTE. */
    void updateSession(Long sessionId, Long unitId, SessionEdit edit, Long teacherId);

    /** Currículo y planificación que necesita el generador para esta sesión. */
    Optional<DraftContext> findDraftContext(Long sessionId);

    /**
     * Guarda el borrador generado: actividades con origen GENERADA y la sesión
     * vuelve a BORRADOR sin aprobación, para que el docente la revise.
     */
    void saveGeneratedDraft(Long sessionId, Long unitId, GeneratedDraft draft);
}
