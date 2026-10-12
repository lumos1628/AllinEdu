package com.edufast.infrastructure.persistence.repository;

import com.edufast.infrastructure.persistence.entity.AsignacionRolEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataAsignacionRolRepository extends JpaRepository<AsignacionRolEntity, Long> {

    @EntityGraph(attributePaths = {"rol", "reportaA"})
    List<AsignacionRolEntity> findByCuentaId(Long cuentaId);
}
