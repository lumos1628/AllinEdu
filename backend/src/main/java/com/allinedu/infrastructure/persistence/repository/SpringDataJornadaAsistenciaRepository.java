package com.allinedu.infrastructure.persistence.repository;

import com.allinedu.infrastructure.persistence.entity.JornadaAsistenciaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SpringDataJornadaAsistenciaRepository extends JpaRepository<JornadaAsistenciaEntity, Long> {

    @EntityGraph(attributePaths = "seccion")
    Optional<JornadaAsistenciaEntity> findBySeccionIdAndFecha(Long seccionId, LocalDate fecha);
}