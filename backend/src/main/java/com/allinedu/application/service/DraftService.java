package com.allinedu.application.service;

import com.allinedu.application.dto.ClassroomRequest;
import com.allinedu.application.dto.ClassroomResponse;
import com.allinedu.application.dto.DraftJobResponse;
import com.allinedu.application.dto.DraftRequest;
import com.allinedu.application.dto.KnotAnswerRequest;
import com.allinedu.application.dto.SessionDetailResponse;
import com.allinedu.domain.model.User;

/**
 * El telar: genera borradores de sesión, los revisa con reglas fijas y deja al docente
 * los nudos que tiene que mirar. Todo lo generado nace sin aprobar (Regla 0a).
 */
public interface DraftService {

    /**
     * Propone (o rehace) la sesión completa. Se puede pedir mientras el docente no haya
     * escrito nada en ella; las respuestas "Cámbialo" a los nudos y el texto de la petición
     * van al generador como indicaciones.
     */
    SessionDetailResponse generateDraft(User user, Long sessionId, DraftRequest request);

    /** Prepara en segundo plano todas las sesiones de la unidad que no tienen secuencia. */
    DraftJobResponse prepareUnit(User user, Long unitId);

    /** Cómo va la preparación de la unidad. */
    DraftJobResponse getUnitJob(User user, Long unitId);

    /** El docente responde un nudo: "Está bien" o "Cámbialo" (y opcionalmente lo recuerda en "Mi aula"). */
    SessionDetailResponse answerKnot(User user, Long sessionId, Long knotId, KnotAnswerRequest request);

    ClassroomResponse getClassroom(User user);

    ClassroomResponse saveClassroom(User user, ClassroomRequest request);
}
