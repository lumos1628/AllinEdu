package com.allinedu.application.dto;

import com.allinedu.domain.model.Section;

public record SectionResponse(
        Long id,
        String nombre,
        String grado,
        String descripcion) {

    public static SectionResponse from(Section section) {
        return new SectionResponse(
                section.getId(),
                section.getNombre(),
                section.getGradoNombre(),
                section.getDescripcion());
    }
}