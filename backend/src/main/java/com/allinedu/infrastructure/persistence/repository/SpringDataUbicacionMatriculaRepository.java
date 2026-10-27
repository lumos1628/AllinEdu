package com.allinedu.infrastructure.persistence.repository;

import com.allinedu.infrastructure.persistence.entity.UbicacionMatriculaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataUbicacionMatriculaRepository extends JpaRepository<UbicacionMatriculaEntity, Long> {

    @EntityGraph(attributePaths = {"matricula", "matricula.estudiante", "matricula.estudiante.persona"})
    List<UbicacionMatriculaEntity> findBySeccionIdAndFechaFinIsNull(Long seccionId);

    @EntityGraph(attributePaths = {"matricula", "matricula.estudiante", "matricula.estudiante.persona"})
    Optional<UbicacionMatriculaEntity> findFirstByMatriculaEstudianteIdAndSeccionIdAndFechaFinIsNull(
            Long estudianteId, Long seccionId);
}