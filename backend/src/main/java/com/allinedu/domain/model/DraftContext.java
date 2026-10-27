package com.allinedu.domain.model;

import java.util.List;

/**
 * Lo único que recibe el generador de borradores: currículo y planificación.
 * Nunca lleva datos de estudiantes.
 */
public record DraftContext(
        String grade,
        List<String> areas,
        String unitTitle,
        String unitSituation,
        String unitPurpose,
        String sessionTitle,
        String sessionPurpose,
        int durationMinutes,
        List<Competency> competencies,
        List<String> criteria,
        List<String> didacticSteps) {

    public record Competency(String name, List<String> capacities, List<Performance> performances) {
    }

    /** Desempeño del grado con la página del Programa Curricular de donde sale. */
    public record Performance(String description, Integer page, boolean requiresConcreteMaterial) {
    }
}
