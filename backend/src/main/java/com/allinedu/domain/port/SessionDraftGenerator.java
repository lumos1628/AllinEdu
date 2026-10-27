package com.allinedu.domain.port;

import com.allinedu.domain.model.DraftContext;
import com.allinedu.domain.model.GeneratedDraft;

/**
 * PUERTO: un servicio externo que propone la secuencia de una sesión.
 * El proveedor (Gemini, Claude, un modelo local) es un detalle de infraestructura.
 */
public interface SessionDraftGenerator {

    /** false si el servidor no tiene el generador configurado. */
    boolean isAvailable();

    GeneratedDraft generate(DraftContext context);
}
