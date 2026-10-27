package com.allinedu.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * Una sesión de aprendizaje completa, en el orden del formato de la institución:
 * propósitos y evidencias, preparación y secuencia didáctica, más su instrumento.
 * status es BORRADOR hasta que el docente la aprueba (Regla 0a).
 */
public record SessionDetail(
        Long id,
        Long unitId,
        String unitTitle,
        int number,
        String title,
        String purpose,
        Integer durationMinutes,
        String status,
        boolean generated,
        Instant editedAt,
        Instant approvedAt,
        List<String> competencies,
        List<String> criteria,
        List<Evidence> evidences,
        Instrument instrument,
        List<Note> dua,
        List<Note> approaches,
        List<Material> materials,
        List<Activity> activities) {

    public static final String DRAFT = "BORRADOR";
    public static final String APPROVED = "PUBLICADA";

    public boolean isDraft() {
        return DRAFT.equals(status);
    }

    public record Evidence(String description, String format) {
    }

    public record Instrument(String name, String type, List<InstrumentCriterion> criteria) {
    }

    public record InstrumentCriterion(String description, List<Level> levels) {
    }

    public record Level(String code, String description) {
    }

    public record Note(String title, String text) {
    }

    public record Material(String name, Integer quantity, String character, String description) {
    }

    /** generated: la propuso un generador y el docente todavía no la cambió. */
    public record Activity(Long id, int order, String moment, String title, String instructions, Integer minutes,
                           boolean generated) {
    }
}
