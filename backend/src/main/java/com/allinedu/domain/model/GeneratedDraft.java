package com.allinedu.domain.model;

import java.util.List;

/**
 * Borrador propuesto por un generador. Nace sin aprobar (Regla 0a).
 */
public record GeneratedDraft(String evidence, List<Activity> activities) {

    public record Activity(String moment, String title, String instructions, Integer minutes) {
    }

    /** Un borrador sirve si trae actividades válidas en los momentos de la sesión. */
    public boolean isUsable() {
        return activities != null && !activities.isEmpty() && activities.stream().allMatch(a ->
                SessionEdit.MOMENTS.contains(a.moment())
                        && a.instructions() != null && !a.instructions().isBlank()
                        && (a.minutes() == null || a.minutes() > 0));
    }
}
