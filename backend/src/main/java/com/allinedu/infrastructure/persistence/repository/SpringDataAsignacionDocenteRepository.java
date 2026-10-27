package com.allinedu.infrastructure.persistence.repository;

import com.allinedu.infrastructure.persistence.entity.AsignacionDocenteEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataAsignacionDocenteRepository extends JpaRepository<AsignacionDocenteEntity, Long> {

    @EntityGraph(attributePaths = {"seccion", "seccion.grado"})
    List<AsignacionDocenteEntity> findByCuentaIdAndVigenteHastaIsNull(Long cuentaId);

    @EntityGraph(attributePaths = {"seccion", "seccion.grado"})
    List<AsignacionDocenteEntity> findByCuentaIdAndSeccionIdAndVigenteHastaIsNull(
            Long cuentaId, Long seccionId);
}