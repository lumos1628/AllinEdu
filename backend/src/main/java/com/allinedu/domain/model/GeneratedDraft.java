package com.allinedu.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Borrador propuesto por un generador. Nace sin aprobar (Regla 0a).
 *
 * @param assumptions lo que el generador tuvo que suponer de la comunidad o los recursos;
 *                    cada uno se vuelve un nudo que el docente confirma o cambia
 * @param model       el modelo que lo produjo, y promptVersion la versión de las instrucciones:
 *                    se guardan para poder medir qué combinación ayuda más
 */
public record GeneratedDraft(String evidence, List<Activity> activities, List<Dua> dua, List<Material> materials,
                             List<Assumption> assumptions, String model, String promptVersion) {

    public GeneratedDraft {
        activities = activities == null ? List.of() : List.copyOf(activities);
        dua = dua == null ? List.of() : List.copyOf(dua);
        materials = materials == null ? List.of() : List.copyOf(materials);
        assumptions = assumptions == null ? List.of() : List.copyOf(assumptions);
    }

    /** step: número del paso didáctico que trabaja la actividad (empieza en 1), o null. */
    public record Activity(String moment, String title, String instructions, Integer minutes, Integer step) {
    }

    /** principle: REPRESENTACION, ACCION_EXPRESION o COMPROMISO, como en pautas_dua. */
    public record Dua(String principle, String text) {
    }

    public record Material(String name, Integer quantity, boolean required) {
    }

    /** phrase: la frase del borrador donde se usa lo supuesto, para señalarla en el documento. */
    public record Assumption(String text, String phrase) {
    }

    public static final List<String> DUA_PRINCIPLES = List.of("REPRESENTACION", "ACCION_EXPRESION", "COMPROMISO");

    /** Un borrador sirve si trae actividades válidas en los momentos de la sesión. */
    public boolean isUsable() {
        return !activities.isEmpty() && activities.stream().allMatch(a ->
                SessionEdit.MOMENTS.contains(a.moment())
                        && a.instructions() != null && !a.instructions().isBlank()
                        && (a.minutes() == null || a.minutes() > 0));
    }

    public int totalMinutes() {
        return activities.stream().map(Activity::minutes).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
    }
}
