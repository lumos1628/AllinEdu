package com.allinedu.domain.model;

import java.util.Set;

/**
 * Nudo: algo de un borrador que necesita la mirada del docente. Se responde con
 * "Está bien" (ACEPTADO) o "Cámbialo" (CAMBIO, con lo que quiere en su lugar).
 * Es la revisión por excepción de EPICAS.md §2: el docente contesta pocas preguntas
 * en lugar de leer y corregir el documento completo.
 *
 * @param fragment frase del borrador que causa el nudo, para señalarla en el documento (puede ser null)
 */
public record Knot(Long id, String type, String question, String fragment, String state, String answer) {

    public static final String MINUTES = "MINUTOS";
    public static final String STEPS = "PASOS";
    public static final String CRITERIA = "CRITERIOS";
    public static final String MATERIAL = "MATERIAL";
    public static final String ASSUMPTION = "SUPUESTO";

    public static final String PENDING = "PENDIENTE";
    public static final String ACCEPTED = "ACEPTADO";
    public static final String CHANGE = "CAMBIO";

    public static final Set<String> ANSWERS = Set.of(ACCEPTED, CHANGE);

    public static Knot open(String type, String question, String fragment) {
        return new Knot(null, type, question, fragment, PENDING, null);
    }

    public boolean isPending() {
        return PENDING.equals(state);
    }

    /** Los nudos de reglas fijas se pueden corregir solos; un supuesto solo lo resuelve el docente. */
    public boolean isRuleBased() {
        return !ASSUMPTION.equals(type);
    }

    /** Lo que el docente pidió, en una frase que el generador entiende. */
    public String asIndication() {
        String base = question.endsWith(".") ? question : question + ".";
        return answer == null || answer.isBlank()
                ? base + " La docente pidió cambiarlo."
                : base + " La docente pidió: " + answer.trim();
    }
}
