package com.allinedu.infrastructure.persistence.repository;

import com.allinedu.infrastructure.persistence.entity.EstudianteEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataEstudianteRepository extends JpaRepository<EstudianteEntity, Long> {

    @EntityGraph(attributePaths = "persona")
    Optional<EstudianteEntity> findWithPersonaById(Long id);
}