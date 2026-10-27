package com.allinedu.infrastructure.persistence.mapper;

import com.allinedu.domain.model.Section;
import com.allinedu.infrastructure.persistence.entity.SeccionEntity;

/**
 * Traductor entre SeccionEntity y el modelo de dominio Section.
 */
public final class SectionMapper {

    private SectionMapper() {
    }

    public static Section toDomain(SeccionEntity entity) {
        return new Section(
                entity.getId(),
                entity.getNombre(),
                entity.getGrado().getNombre(),
                entity.getGrado().getCodigo());
    }
}