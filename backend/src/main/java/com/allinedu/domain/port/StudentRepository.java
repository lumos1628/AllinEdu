package com.allinedu.domain.port;

import com.allinedu.domain.model.Student;

import java.util.List;
import java.util.Optional;

/**
 * PUERTO: el contrato que el negocio necesita para consultar estudiantes.
 */
public interface StudentRepository {

    Optional<Student> findById(Long id);

    List<Student> findBySectionId(Long sectionId);
}