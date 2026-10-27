package com.allinedu.domain.port;

import com.allinedu.domain.model.Section;

import java.util.List;
import java.util.Optional;

/**
 * PUERTO: el contrato que el negocio necesita para consultar secciones.
 */
public interface SectionRepository {

    List<Section> findByTeacherId(Long teacherId);

    /**
     * Devuelve la sección solo si está asignada al docente indicado.
     */
    Optional<Section> findAssignedToTeacher(Long teacherId, Long sectionId);
}