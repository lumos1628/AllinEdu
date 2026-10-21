package com.edufast.application.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Lo necesario para preparar una clase: el bloque, la unidad que cubre la fecha
 * y las sesiones de esa unidad que todavía no tienen fecha.
 */
public record SlotOptionsResponse(
        Long slotId,
        LocalDate date,
        String area,
        String start,
        String end,
        Long unitId,
        String unitTitle,
        Long existingSessionId,
        List<UnitDetailResponse.Session> candidates) {
}
