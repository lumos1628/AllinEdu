package com.edufast.application.service;

import com.edufast.application.dto.SessionDetailResponse;
import com.edufast.domain.model.User;

public interface DraftService {

    /**
     * Pide al generador un borrador de la secuencia para una sesión sin secuencia.
     * El resultado queda como borrador generado, sin aprobar (Regla 0a).
     */
    SessionDetailResponse generateDraft(User user, Long sessionId);
}
