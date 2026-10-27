package com.allinedu.application.service;

import com.allinedu.application.dto.SessionDetailResponse;
import com.allinedu.domain.model.User;

public interface DraftService {

    /**
     * Pide al generador un borrador de la secuencia para una sesión sin secuencia.
     * El resultado queda como borrador generado, sin aprobar (Regla 0a).
     */
    SessionDetailResponse generateDraft(User user, Long sessionId);
}
