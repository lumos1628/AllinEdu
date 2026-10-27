package com.allinedu.infrastructure.persistence.repository;

import com.allinedu.infrastructure.persistence.entity.CuentaUsuarioEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataCuentaUsuarioRepository extends JpaRepository<CuentaUsuarioEntity, Long> {

    @EntityGraph(attributePaths = "persona")
    Optional<CuentaUsuarioEntity> findByCorreo(String correo);
}