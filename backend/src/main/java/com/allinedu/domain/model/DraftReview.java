package com.allinedu.domain.model;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Las reglas fijas que revisan un borrador antes de que lo vea el docente.
 * Cada hallazgo es a la vez un nudo (la pregunta para el docente) y una corrección
 * (la instrucción para un segundo intento del generador). No usa IA: lo que se puede
 * comprobar con certeza no se le pregunta al modelo.
 */
public final class DraftReview {

    /** Más supuestos que esto ya no es revisión por excepción. */
    static final int MAX_ASSUMPTIONS = 3;

    private DraftReview() {
    }

    private record Finding(Knot knot, String correction) {
    }

    public static List<Knot> knotsOf(DraftContext context, GeneratedDraft draft) {
        List<Knot> knots = new ArrayList<>(findings(context, draft).stream().map(Finding::knot).toList());
        knots.addAll(assumptions(draft, context.classroom()));
        return List.copyOf(knots);
    }

    /** Instrucciones para corregir lo que las reglas encontraron; vacío si no hay nada que corregir. */
    public static List<String> corrections(DraftContext context, GeneratedDraft draft) {
        return findings(context, draft).stream().map(Finding::correction).toList();
    }

    public static long ruleKnots(List<Knot> knots) {
        return knots.stream().filter(Knot::isRuleBased).count();
    }

    private static List<Finding> findings(DraftContext c, GeneratedDraft d) {
        List<Finding> found = new ArrayList<>();

        int total = d.totalMinutes();
        if (total != c.durationMinutes()) {
            found.add(new Finding(
                    Knot.open(Knot.MINUTES, total == 0
                            ? "Las actividades no dicen cuánto dura cada una."
                            : "Las actividades suman " + total + " minutos y tu clase dura " + c.durationMinutes() + ".", null),
                    "Ajusta los minutos de las actividades para que sumen exactamente " + c.durationMinutes() + "."));
        }

        List<GeneratedDraft.Activity> development = d.activities().stream()
                .filter(a -> "DESARROLLO".equals(a.moment())).toList();
        List<String> missing = IntStream.range(0, c.didacticSteps().size())
                .filter(i -> development.stream().noneMatch(a -> Objects.equals(a.step(), i + 1)
                        || plain(a.title()).equals(plain(c.didacticSteps().get(i).name()))))
                .mapToObj(i -> c.didacticSteps().get(i).name())
                .toList();
        List<Integer> order = development.stream().map(GeneratedDraft.Activity::step)
                .filter(s -> c.step(s) != null).toList();
        if (!missing.isEmpty()) {
            found.add(new Finding(
                    Knot.open(Knot.STEPS, "El desarrollo no pasa por " + quoted(missing, "ni")
                            + (missing.size() == 1 ? ", un paso" : ", pasos") + " que usa tu institución.", null),
                    "En el DESARROLLO incluye una actividad para cada uno de estos pasos de la institución, "
                            + "en su orden, e indica su número en «paso»: " + quoted(missing, "y") + "."));
        } else if (!IntStream.range(1, order.size()).allMatch(i -> order.get(i) >= order.get(i - 1))) {
            found.add(new Finding(
                    Knot.open(Knot.STEPS, "Los pasos del desarrollo van en otro orden que el de tu institución.", null),
                    "Ordena las actividades del DESARROLLO según el orden de los pasos de la institución."));
        }

        boolean criteriaShared = d.activities().stream()
                .filter(a -> "INICIO".equals(a.moment()))
                .anyMatch(a -> plain(a.title() + " " + a.instructions()).contains("criterio"));
        if (!c.criteria().isEmpty() && !criteriaShared) {
            found.add(new Finding(
                    Knot.open(Knot.CRITERIA,
                            "Al empezar no se les dice a los niños qué se va a observar (los criterios de evaluación).", null),
                    "En el INICIO agrega comunicar a los estudiantes el propósito y los criterios de evaluación."));
        }

        DraftContext.Performance source = c.concreteMaterialSource();
        if (source != null && d.materials().isEmpty()) {
            String page = source.page() == null || source.page().isBlank()
                    ? "" : " (Programa Curricular, p. " + source.page() + ")";
            found.add(new Finding(
                    Knot.open(Knot.MATERIAL, "El desempeño pide material concreto" + page
                            + " y el borrador no usa ninguno.", null),
                    "Usa material concreto en el desarrollo y anótalo en «materiales» como obligatorio."));
        }
        return found;
    }

    private static List<Knot> assumptions(GeneratedDraft d, String classroom) {
        return d.assumptions().stream()
                .filter(a -> a.text() != null && !a.text().isBlank())
                .filter(a -> !alreadyKnown(a.text(), classroom))
                .limit(MAX_ASSUMPTIONS)
                .map(a -> Knot.open(Knot.ASSUMPTION, supposed(a.text()), fragmentIn(d, a.phrase())))
                .toList();
    }

    // Fórmulas con las que el generador suele empezar un supuesto; se quitan para no decir
    // "Supuse que se asume que...".
    private static final List<String> LEADS = List.of(
            "supuse que", "se asume que", "asumi que", "asumo que", "se supone que", "supongo que",
            "suponemos que", "asumimos que", "se considera que");

    private static String supposed(String text) {
        String t = text.trim();
        String p = plain(t);
        for (String lead : LEADS) {
            if (p.startsWith(lead)) {
                t = t.substring(lead.length()).trim();
                break;
            }
        }
        if (t.isEmpty()) {
            t = text.trim();
        }
        String body = Character.toLowerCase(t.charAt(0)) + t.substring(1);
        return "Supuse que " + (body.endsWith(".") ? body : body + ".");
    }

    // Solo se señala una frase que de verdad está en el borrador.
    private static String fragmentIn(GeneratedDraft d, String phrase) {
        if (phrase == null || phrase.isBlank()) {
            return null;
        }
        String p = phrase.trim();
        return d.activities().stream().anyMatch(a -> a.instructions().contains(p)) ? p : null;
    }

    // Palabras que no dicen nada del lugar: no cuentan para decidir si un supuesto ya se sabía.
    private static final Set<String> GENERIC = Set.of(
            "aula", "escue", "estud", "docen", "tiene", "cuent", "exist", "lugar", "donde", "reali",
            "para", "pero", "como", "esta", "este", "esto", "sobr", "ellos", "ellas", "todos", "nuest");

    /**
     * true si el supuesto repite lo que el docente ya contó en "Mi aula". El generador a veces
     * lo declara igual; preguntarle al docente lo que ya dijo es carga, no revisión.
     */
    static boolean alreadyKnown(String assumption, String classroom) {
        if (classroom == null || classroom.isBlank()) {
            return false;
        }
        Set<String> known = stems(classroom);
        Set<String> said = stems(assumption);
        if (said.size() < 2) {
            return false;
        }
        long covered = said.stream().filter(known::contains).count();
        return covered * 4 >= said.size() * 3;
    }

    // Raíces de 5 letras de las palabras con contenido: "domingos" y "dominical" comparten "domin".
    private static Set<String> stems(String text) {
        Set<String> out = new HashSet<>();
        for (String w : plain(text).split("[^a-zñ]+")) {
            if (w.length() >= 4) {
                String stem = w.length() > 5 ? w.substring(0, 5) : w;
                if (!GENERIC.contains(stem)) {
                    out.add(stem);
                }
            }
        }
        return out;
    }

    private static String quoted(List<String> names, String and) {
        List<String> q = names.stream().map(n -> "«" + n + "»").toList();
        return q.size() == 1 ? q.get(0)
                : String.join(", ", q.subList(0, q.size() - 1)) + " " + and + " " + q.get(q.size() - 1);
    }

    static String plain(String s) {
        if (s == null) {
            return "";
        }
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase().trim();
    }
}
