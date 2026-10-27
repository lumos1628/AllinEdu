package com.allinedu.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo único que recibe el generador de borradores: currículo, planificación y lo que el
 * docente contó de su aula. Nunca lleva datos de estudiantes.
 *
 * @param previousSessions sesiones anteriores de la unidad, para que el borrador continúe donde quedaron
 * @param classroom        "Mi aula": lo que el docente escribió de su comunidad y sus materiales (puede ser vacío)
 * @param indications      lo que el docente pidió cambiar en este borrador (respuestas a nudos o texto libre)
 * @param corrections      lo que las reglas fijas encontraron mal en el intento anterior
 * @param currentDraft     al rehacer: las actividades del borrador vigente, para cambiar solo lo pedido
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
        List<Step> didacticSteps,
        List<String> previousSessions,
        String classroom,
        List<String> indications,
        List<String> corrections,
        List<String> currentDraft) {

    public record Competency(String name, List<String> capacities, List<Performance> performances) {
    }

    /** Desempeño del grado con la página del Programa Curricular de donde sale. */
    public record Performance(Long id, String description, String page, boolean requiresConcreteMaterial) {
    }

    /** Paso del proceso didáctico de la institución para el desarrollo. */
    public record Step(Long id, String name) {
    }

    public DraftContext withIndications(List<String> more) {
        return new DraftContext(grade, areas, unitTitle, unitSituation, unitPurpose, sessionTitle, sessionPurpose,
                durationMinutes, competencies, criteria, didacticSteps, previousSessions, classroom,
                concat(indications, more), corrections, currentDraft);
    }

    public DraftContext withCorrections(List<String> fixes) {
        return new DraftContext(grade, areas, unitTitle, unitSituation, unitPurpose, sessionTitle, sessionPurpose,
                durationMinutes, competencies, criteria, didacticSteps, previousSessions, classroom,
                indications, concat(corrections, fixes), currentDraft);
    }

    public DraftContext withCurrentDraft(List<String> activities) {
        return new DraftContext(grade, areas, unitTitle, unitSituation, unitPurpose, sessionTitle, sessionPurpose,
                durationMinutes, competencies, criteria, didacticSteps, previousSessions, classroom,
                indications, corrections, List.copyOf(activities));
    }

    /** El primer desempeño que exige material concreto: es la fuente de un material obligatorio. */
    public Performance concreteMaterialSource() {
        return competencies.stream()
                .flatMap(c -> c.performances().stream())
                .filter(Performance::requiresConcreteMaterial)
                .findFirst()
                .orElse(null);
    }

    /** El paso con ese número (empieza en 1), o null si el número no corresponde a ninguno. */
    public Step step(Integer number) {
        if (number == null || number < 1 || number > didacticSteps.size()) {
            return null;
        }
        return didacticSteps.get(number - 1);
    }

    private static List<String> concat(List<String> a, List<String> b) {
        List<String> all = new ArrayList<>(a == null ? List.of() : a);
        if (b != null) {
            b.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).forEach(all::add);
        }
        return List.copyOf(all);
    }
}
