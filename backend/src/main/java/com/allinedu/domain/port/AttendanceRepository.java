package com.allinedu.domain.port;

import com.allinedu.domain.model.Jornada;

import java.time.LocalDate;
import java.util.Optional;

/**
 * PUERTO: el contrato que el negocio necesita para persistir asistencia.
 */
public interface AttendanceRepository {

    Optional<Jornada> findBySectionAndDate(Long sectionId, LocalDate date);

    /**
     * Guarda la jornada (cabecera) y sus registros de asistencia.
     */
    Jornada save(Jornada jornada, Long registeredBy, boolean confirm);
}