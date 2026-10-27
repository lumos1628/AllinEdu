package com.allinedu.infrastructure.ai;

import com.allinedu.domain.model.DraftContext;
import com.allinedu.domain.model.GeneratedDraft;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GeminiSessionDraftGeneratorTest {

    private static String response(String draftJson) {
        return JsonMapper.builder().build().writeValueAsString(Map.of("candidates", List.of(Map.of(
                "content", Map.of("parts", List.of(Map.of("text", draftJson)))))));
    }

    @Test
    void leeLaSesionCompletaConPasosDuaMaterialesYSupuestos() {
        String json = """
                {"evidencia": "Ficha resuelta",
                 "actividades": [
                   {"momento": "INICIO", "titulo": "Propósito", "instrucciones": "Se comunican los criterios.", "minutos": 15, "paso": 0},
                   {"momento": "DESARROLLO", "titulo": "Reparto", "instrucciones": "Reparten en la feria.", "minutos": 75, "paso": 2}],
                 "dua": {"representacion": "Con tiras.", "accion_expresion": "Oral o escrito.", "compromiso": " "},
                 "materiales": [{"nombre": "Tiras de papel", "cantidad": 26, "obligatorio": true}],
                 "supuestos": [{"supuesto": "Hay una feria cerca", "frase": "en la feria"}]}
                """;

        GeneratedDraft d = GeminiSessionDraftGenerator.parse(response(json), "gemini-x");

        assertTrue(d.isUsable());
        assertNull(d.activities().get(0).step());
        assertEquals(2, d.activities().get(1).step());
        assertEquals(90, d.totalMinutes());
        assertEquals("Con tiras.", d.dua().get(0).text());
        assertNull(d.dua().get(2).text());
        assertTrue(d.materials().get(0).required());
        assertEquals("en la feria", d.assumptions().get(0).phrase());
        assertEquals("gemini-x", d.model());
        assertEquals(GeminiSessionDraftGenerator.PROMPT_VERSION, d.promptVersion());
    }

    @Test
    void elPromptLlevaMiAulaLasIndicacionesYLosPasosNumerados() {
        var c = new DraftContext("4.º de primaria", List.of("Matemática"), "Repartimos", null, null, "Fracciones", null, 90,
                List.of(), List.of(), List.of(new DraftContext.Step(1L, "Familiarización")),
                List.of("Sesión 1: Contamos"), "Hay una feria los domingos.", List.of("Más corta"), List.of("Suma 90"),
                List.of("[DESARROLLO] Van a la feria (90 min)"));

        String p = GeminiSessionDraftGenerator.prompt(c);

        assertTrue(p.contains("1. Familiarización"));
        assertTrue(p.contains("Sesión 1: Contamos"));
        assertTrue(p.contains("Hay una feria los domingos."));
        assertTrue(p.contains("- Más corta"));
        assertTrue(p.contains("- Suma 90"));
        assertTrue(p.contains("Conserva lo que las indicaciones no piden cambiar"));
    }
}
