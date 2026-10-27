package com.allinedu.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DraftReviewTest {

    private static final List<DraftContext.Step> STEPS = List.of(
            new DraftContext.Step(11L, "Familiarización con el problema"),
            new DraftContext.Step(12L, "Búsqueda de estrategias"),
            new DraftContext.Step(13L, "Formalización"));

    private static DraftContext context(List<String> criteria, List<DraftContext.Step> steps, boolean concrete) {
        var performance = new DraftContext.Performance(40L, "Representa fracciones con material concreto.", "238", concrete);
        return new DraftContext("4.º de primaria", List.of("Matemática"), "Repartimos", null, null, "Fracciones", null, 90,
                List.of(new DraftContext.Competency("Resuelve problemas de cantidad", List.of(), List.of(performance))),
                criteria, steps, List.of(), "", List.of(), List.of(), List.of());
    }

    private static GeneratedDraft.Activity act(String moment, String instructions, int minutes, Integer step) {
        return new GeneratedDraft.Activity(moment, null, instructions, minutes, step);
    }

    private static GeneratedDraft draft(List<GeneratedDraft.Activity> activities, List<GeneratedDraft.Material> materials,
                                        List<GeneratedDraft.Assumption> assumptions) {
        return new GeneratedDraft("Ficha", activities, List.of(), materials, assumptions, "m", "v");
    }

    private static final List<GeneratedDraft.Activity> GOOD = List.of(
            act("INICIO", "Se comunican el propósito y los criterios de evaluación.", 15, null),
            act("DESARROLLO", "Leen el problema del reparto.", 20, 1),
            act("DESARROLLO", "Prueban repartir con tiras de papel.", 25, 2),
            act("DESARROLLO", "Escriben la fracción de cada parte.", 15, 3),
            act("CIERRE", "Comentan qué aprendieron.", 15, null));

    private static final List<GeneratedDraft.Material> STRIPS = List.of(new GeneratedDraft.Material("Tiras de papel", 26, true));

    @Test
    void unBorradorQueCumpleNoTieneNudos() {
        var c = context(List.of("Representa el reparto"), STEPS, true);

        assertTrue(DraftReview.knotsOf(c, draft(GOOD, STRIPS, List.of())).isEmpty());
        assertTrue(DraftReview.corrections(c, draft(GOOD, STRIPS, List.of())).isEmpty());
    }

    @Test
    void minutosQueNoCuadranConLaClase() {
        var c = context(List.of(), List.of(), false);
        var d = draft(List.of(act("INICIO", "Saludo.", 60, null), act("CIERRE", "Cierre.", 45, null)), List.of(), List.of());

        Knot k = DraftReview.knotsOf(c, d).get(0);

        assertEquals(Knot.MINUTES, k.type());
        assertTrue(k.question().contains("105 minutos") && k.question().contains("90"));
        assertTrue(DraftReview.corrections(c, d).get(0).contains("exactamente 90"));
    }

    @Test
    void unPasoDeLaInstitucionQueFalta() {
        var c = context(List.of(), STEPS, false);
        var d = draft(List.of(
                act("DESARROLLO", "Leen el problema.", 45, 1),
                act("DESARROLLO", "Formalizan.", 45, 3)), List.of(), List.of());

        List<Knot> knots = DraftReview.knotsOf(c, d);

        assertEquals(Knot.STEPS, knots.get(0).type());
        assertTrue(knots.get(0).question().contains("«Búsqueda de estrategias»"));
    }

    @Test
    void pasosEnOtroOrden() {
        var c = context(List.of(), STEPS, false);
        var d = draft(List.of(
                act("DESARROLLO", "Formalizan.", 30, 3),
                act("DESARROLLO", "Leen el problema.", 30, 1),
                act("DESARROLLO", "Prueban.", 30, 2)), List.of(), List.of());

        Knot k = DraftReview.knotsOf(c, d).get(0);

        assertEquals(Knot.STEPS, k.type());
        assertTrue(k.question().contains("otro orden"));
    }

    @Test
    void losCriteriosNoSeComunicanAlEmpezar() {
        var c = context(List.of("Explica cómo comprobó"), List.of(), false);
        var d = draft(List.of(act("INICIO", "Saludo y oración.", 15, null), act("DESARROLLO", "Trabajan.", 75, null)),
                List.of(), List.of());

        assertEquals(Knot.CRITERIA, DraftReview.knotsOf(c, d).get(0).type());
    }

    @Test
    void elDesempenoPideMaterialConcretoYNoHay() {
        var c = context(List.of(), List.of(), true);
        var d = draft(List.of(act("DESARROLLO", "Resuelven en el cuaderno.", 90, null)), List.of(), List.of());

        Knot k = DraftReview.knotsOf(c, d).get(0);

        assertEquals(Knot.MATERIAL, k.type());
        assertTrue(k.question().contains("p. 238"));
    }

    @Test
    void losSupuestosSeVuelvenPreguntasYSoloSeSenalaUnaFraseQueExiste() {
        var c = context(List.of(), List.of(), false);
        var d = draft(List.of(act("DESARROLLO", "Recuerdan lo que vieron en el mercado de abastos.", 90, null)), List.of(),
                List.of(new GeneratedDraft.Assumption("Hay un mercado de abastos cerca", "el mercado de abastos"),
                        new GeneratedDraft.Assumption("Las familias tienen balanza", "una balanza de cocina"),
                        new GeneratedDraft.Assumption("Hay patio", null),
                        new GeneratedDraft.Assumption("Hay biblioteca", null)));

        List<Knot> knots = DraftReview.knotsOf(c, d);

        assertEquals(3, knots.size());
        assertEquals("Supuse que hay un mercado de abastos cerca.", knots.get(0).question());
        assertEquals("el mercado de abastos", knots.get(0).fragment());
        assertNull(knots.get(1).fragment());
        assertTrue(DraftReview.corrections(c, d).isEmpty());
    }

    @Test
    void noPreguntaLoQueElDocenteYaContoEnMiAula() {
        String classroom = "Los domingos hay una feria en la losa deportiva. Hacemos las tiras con papel.";

        assertTrue(DraftReview.alreadyKnown("La losa deportiva es el lugar donde se realiza la feria dominical", classroom));
        assertTrue(DraftReview.alreadyKnown("Hay una feria en la losa deportiva los domingos", classroom));
        assertFalse(DraftReview.alreadyKnown("Existe una lista de precios de dos puestos de la feria", classroom));
        assertFalse(DraftReview.alreadyKnown("Las ollas de la feria son del mismo tamaño", classroom));
        assertFalse(DraftReview.alreadyKnown("Hay una feria los domingos", ""));
    }

    @Test
    void unSupuestoQueEmpiezaConSeAsumeNoSeRepite() {
        var c = context(List.of(), List.of(), false);
        var d = draft(List.of(act("DESARROLLO", "Usan tiras.", 90, null)), List.of(),
                List.of(new GeneratedDraft.Assumption("Se asume que el aula tiene tiras de fracciones", null)));

        assertEquals("Supuse que el aula tiene tiras de fracciones.", DraftReview.knotsOf(c, d).get(0).question());
    }
}
