package com.allinedu.application.dto;

import jakarta.validation.constraints.Size;

/** Pedir otro borrador. indications: lo que el docente quiere distinto, con sus palabras (opcional). */
public record DraftRequest(@Size(max = 1000) String indications) {
}
