package com.allinedu.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Un borrador listo para guardar: lo que propuso el generador, los nudos que el docente
 * va a revisar y cómo se produjo (intentos, duración, indicaciones), para poder medirlo.
 *
 * @param stepIds                    el paso didáctico de cada actividad, en el mismo orden (null si ninguno)
 * @param concreteMaterialSourceId   el desempeño que vuelve obligatorio un material concreto (null si ninguno)
 */
public record WovenDraft(GeneratedDraft draft, List<Knot> knots, List<Long> stepIds, Long concreteMaterialSourceId,
                         List<String> indications, int attempts, long durationMs) {

    public static WovenDraft of(DraftContext context, GeneratedDraft draft, List<Knot> knots, int attempts,
                                long durationMs) {
        List<Long> steps = new ArrayList<>();
        for (GeneratedDraft.Activity a : draft.activities()) {
            DraftContext.Step step = "DESARROLLO".equals(a.moment()) ? context.step(a.step()) : null;
            steps.add(step == null ? null : step.id());
        }
        DraftContext.Performance source = context.concreteMaterialSource();
        return new WovenDraft(draft, List.copyOf(knots), Collections.unmodifiableList(steps),
                source == null ? null : source.id(), context.indications(), attempts, durationMs);
    }

    /** Las indicaciones del docente en un solo texto, como se guardan junto al borrador. */
    public String indicationsText() {
        return indications.isEmpty() ? null : String.join("\n", indications);
    }
}
