package com.allinedu.domain.port;

import com.allinedu.domain.model.DraftContext;
import com.allinedu.domain.model.Knot;
import com.allinedu.domain.model.WovenDraft;

import java.util.List;
import java.util.Optional;

/**
 * PUERTO: lo que necesita el telar (la generación de borradores) para leer su contexto,
 * guardar lo generado con sus nudos y recordar lo que el docente contó de su aula.
 */
public interface DraftRepository {

    /** Currículo, planificación, sesiones anteriores y "Mi aula" del docente para esta sesión. */
    Optional<DraftContext> findDraftContext(Long sessionId, Long teacherId);

    /**
     * Guarda el borrador: actividades, DUA y materiales con origen GENERADA, el registro de la
     * generación y sus nudos. La sesión vuelve a BORRADOR sin aprobación (Regla 0a).
     */
    void saveGeneratedDraft(Long sessionId, Long unitId, Long teacherId, WovenDraft woven);

    /**
     * Sesiones en borrador de la unidad que todavía no tienen secuencia, en orden. Una sesión
     * aprobada no entra al lote aunque esté vacía: esa decisión ya la tomó el docente.
     */
    List<Long> findSessionsWithoutSequence(Long unitId);

    /** Los nudos del último borrador de la sesión. */
    List<Knot> findKnots(Long sessionId);

    /** Registra la respuesta del docente. false si el nudo no es de esa sesión o ya no está vigente. */
    boolean answerKnot(Long sessionId, Long knotId, String state, String answer, Long teacherId);

    /** "Mi aula" del docente; vacío si nunca la escribió. */
    String findClassroom(Long teacherId);

    void saveClassroom(Long teacherId, String text);
}
