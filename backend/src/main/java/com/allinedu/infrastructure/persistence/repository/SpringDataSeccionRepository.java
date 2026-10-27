package com.allinedu.infrastructure.persistence.repository;

import com.allinedu.infrastructure.persistence.entity.SeccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSeccionRepository extends JpaRepository<SeccionEntity, Long> {
}