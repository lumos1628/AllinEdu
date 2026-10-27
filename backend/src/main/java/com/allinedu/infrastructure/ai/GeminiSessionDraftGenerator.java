package com.allinedu.infrastructure.ai;

import com.allinedu.domain.exception.GeneratorUnavailableException;
import com.allinedu.domain.model.DraftContext;
import com.allinedu.domain.model.GeneratedDraft;
import com.allinedu.domain.port.SessionDraftGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ADAPTADOR del puerto SessionDraftGenerator sobre la API de Gemini.
 * Prueba los modelos configurados en orden: el plan gratuito a veces responde
 * 503 por demanda y otro modelo sí atiende.
 */
@Component
public class GeminiSessionDraftGenerator implements SessionDraftGenerator {

    private static final Logger log = LoggerFactory.getLogger(GeminiSessionDraftGenerator.class);

    /** Cambia cuando cambian las instrucciones o el esquema: se guarda con cada borrador para medir. */
    static final String PROMPT_VERSION = "telar-4";

    private static final String SYSTEM = """
            Eres un asistente que propone BORRADORES de sesiones de aprendizaje para docentes de \
            primaria de escuelas públicas del Perú, según el Currículo Nacional de la Educación Básica (CNEB). \
            La docente revisa y aprueba todo lo que propones.
            Reglas:
            - Escribe en español claro, en tercera persona y sin adornos ("Los estudiantes...", "La docente...").
            - Organiza la sesión en tres momentos: INICIO, DESARROLLO y CIERRE.
            - En el INICIO incluye siempre comunicar el propósito y los criterios de evaluación a los estudiantes.
            - Si la institución tiene pasos didácticos, el DESARROLLO tiene al menos una actividad por paso, \
            en ese orden, y en "paso" va el número del paso que trabaja (0 en INICIO y CIERRE).
            - Si algún desempeño exige material concreto, úsalo en el desarrollo y anótalo como obligatorio.
            - Los minutos de todas las actividades deben sumar exactamente la duración de la sesión.
            - Continúa el hilo de la unidad: retoma lo de las sesiones anteriores cuando ayude.
            - Si la docente contó cómo es su aula y su comunidad, úsalo y no lo supongas. Prefiere los \
            materiales que dijo tener; si no dijo nada, materiales baratos y comunes en una escuela pública.
            - Si tuviste que imaginar algo del lugar, de la comunidad o de los recursos del aula que no está \
            en la información (un mercado, un río, una fiesta, un material que no es común), decláralo en \
            "supuestos". Solo lo que cambiaría la actividad si no fuera cierto. No declares lo que la docente \
            ya contó de su aula, ni materiales que hay en cualquier aula (papel, plumones, colores, tijeras), \
            ni lo que saben los estudiantes. Escribe cada supuesto como un hecho corto ("hay un mercado cerca \
            de la escuela"), sin "se asume" ni "supuse". En "frase" copia un fragmento exacto y corto (de 3 a \
            10 palabras) de la actividad donde lo usas. Como máximo tres; lo normal es ninguno o uno.
            - No escribas etiquetas como "(obligatorio)" o "(material concreto)" dentro de las instrucciones.
            - El DUA son tres frases concretas para esta sesión: cómo se representa el contenido, cómo pueden \
            expresar lo aprendido y cómo se los motiva.
            - Usa solo la información dada. No inventes nombres de estudiantes, datos personales, \
            diagnósticos ni calificaciones: la evaluación y las decisiones las toma la docente.
            - La evidencia de aprendizaje es lo que producen o hacen los estudiantes y permite ver los criterios.
            - Si la docente dio indicaciones para este borrador, cúmplelas aunque cambien lo anterior.
            """;

    private static final Map<String, Object> SCHEMA = Map.of(
            "type", "OBJECT",
            "properties", Map.of(
                    "evidencia", Map.of("type", "STRING"),
                    "actividades", Map.of(
                            "type", "ARRAY",
                            "items", Map.of(
                                    "type", "OBJECT",
                                    "properties", Map.of(
                                            "momento", Map.of("type", "STRING", "enum", List.of("INICIO", "DESARROLLO", "CIERRE")),
                                            "titulo", Map.of("type", "STRING"),
                                            "instrucciones", Map.of("type", "STRING"),
                                            "minutos", Map.of("type", "INTEGER"),
                                            "paso", Map.of("type", "INTEGER")),
                                    "required", List.of("momento", "titulo", "instrucciones", "minutos", "paso"))),
                    "dua", Map.of(
                            "type", "OBJECT",
                            "properties", Map.of(
                                    "representacion", Map.of("type", "STRING"),
                                    "accion_expresion", Map.of("type", "STRING"),
                                    "compromiso", Map.of("type", "STRING")),
                            "required", List.of("representacion", "accion_expresion", "compromiso")),
                    "materiales", Map.of(
                            "type", "ARRAY",
                            "items", Map.of(
                                    "type", "OBJECT",
                                    "properties", Map.of(
                                            "nombre", Map.of("type", "STRING"),
                                            "cantidad", Map.of("type", "INTEGER"),
                                            "obligatorio", Map.of("type", "BOOLEAN")),
                                    "required", List.of("nombre", "obligatorio"))),
                    "supuestos", Map.of(
                            "type", "ARRAY",
                            "items", Map.of(
                                    "type", "OBJECT",
                                    "properties", Map.of(
                                            "supuesto", Map.of("type", "STRING"),
                                            "frase", Map.of("type", "STRING")),
                                    "required", List.of("supuesto", "frase")))),
            "required", List.of("evidencia", "actividades", "dua", "materiales", "supuestos"));

    private final String apiKey;
    private final List<String> models;
    private final RestClient client;
    private final JsonMapper json = JsonMapper.builder().build();

    public GeminiSessionDraftGenerator(
            @Value("${allinedu.ai.gemini.api-key:}") String apiKey,
            @Value("${allinedu.ai.gemini.models:gemini-3.1-flash-lite,gemini-flash-lite-latest}") String models,
            @Value("${allinedu.ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl) {
        this.apiKey = apiKey;
        this.models = Arrays.stream(models.split(",")).map(String::trim).filter(m -> !m.isEmpty()).toList();
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(60));
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank() && !models.isEmpty();
    }

    @Override
    public GeneratedDraft generate(DraftContext context) {
        String body = json.writeValueAsString(Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", SYSTEM))),
                "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", prompt(context))))),
                "generationConfig", Map.of(
                        "temperature", 0.5,
                        "responseMimeType", "application/json",
                        "responseSchema", SCHEMA)));

        for (String model : models) {
            try {
                String response = client.post()
                        .uri("/models/{model}:generateContent", model)
                        .header("x-goog-api-key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(String.class);
                GeneratedDraft draft = parse(response, model);
                if (draft.isUsable()) {
                    return draft;
                }
                log.warn("El modelo {} devolvió un borrador incompleto", model);
            } catch (RestClientException | IllegalArgumentException | tools.jackson.core.JacksonException e) {
                // La clave va en una cabecera: el mensaje de error no la contiene.
                log.warn("El modelo {} no pudo generar el borrador: {}", model, e.getMessage());
            }
        }
        throw new GeneratorUnavailableException(
                "El generador está ocupado o no respondió bien. Intenta de nuevo en un minuto.");
    }

    static GeneratedDraft parse(String response, String model) {
        JsonMapper json = JsonMapper.builder().build();
        JsonNode parts = json.readTree(response).path("candidates").path(0).path("content").path("parts");
        String text = parts.path(parts.size() - 1).path("text").asString();
        JsonNode root = json.readTree(text);

        List<GeneratedDraft.Activity> activities = new ArrayList<>();
        for (JsonNode a : root.path("actividades")) {
            int minutes = a.path("minutos").asInt(0);
            int step = a.path("paso").asInt(0);
            activities.add(new GeneratedDraft.Activity(
                    a.path("momento").asString(),
                    blankToNull(a.path("titulo").asString()),
                    a.path("instrucciones").asString(),
                    minutes > 0 ? minutes : null,
                    step > 0 ? step : null));
        }

        JsonNode dua = root.path("dua");
        List<GeneratedDraft.Dua> principles = List.of(
                new GeneratedDraft.Dua("REPRESENTACION", blankToNull(dua.path("representacion").asString())),
                new GeneratedDraft.Dua("ACCION_EXPRESION", blankToNull(dua.path("accion_expresion").asString())),
                new GeneratedDraft.Dua("COMPROMISO", blankToNull(dua.path("compromiso").asString())));

        List<GeneratedDraft.Material> materials = new ArrayList<>();
        for (JsonNode m : root.path("materiales")) {
            int quantity = m.path("cantidad").asInt(0);
            materials.add(new GeneratedDraft.Material(blankToNull(m.path("nombre").asString()),
                    quantity > 0 ? quantity : null, m.path("obligatorio").asBoolean(false)));
        }

        List<GeneratedDraft.Assumption> assumptions = new ArrayList<>();
        for (JsonNode s : root.path("supuestos")) {
            assumptions.add(new GeneratedDraft.Assumption(blankToNull(s.path("supuesto").asString()),
                    blankToNull(s.path("frase").asString())));
        }

        return new GeneratedDraft(blankToNull(root.path("evidencia").asString()), activities, principles,
                materials, assumptions, model, PROMPT_VERSION);
    }

    static String prompt(DraftContext c) {
        StringBuilder p = new StringBuilder();
        p.append("Propón la sesión completa: secuencia didáctica, evidencia, DUA, materiales y supuestos.\n\n");
        p.append("Grado: ").append(c.grade()).append('\n');
        if (!c.areas().isEmpty()) p.append("Área: ").append(String.join(", ", c.areas())).append('\n');
        p.append("Unidad: ").append(c.unitTitle()).append('\n');
        if (c.unitSituation() != null) p.append("Situación significativa: ").append(c.unitSituation()).append('\n');
        if (c.unitPurpose() != null) p.append("Propósito de la unidad: ").append(c.unitPurpose()).append('\n');
        if (!c.previousSessions().isEmpty()) {
            p.append("Sesiones anteriores de la unidad:\n");
            c.previousSessions().forEach(s -> p.append("- ").append(s).append('\n'));
        }
        p.append("\nSesión: ").append(c.sessionTitle()).append('\n');
        if (c.sessionPurpose() != null) p.append("Propósito de la sesión: ").append(c.sessionPurpose()).append('\n');
        p.append("Duración: ").append(c.durationMinutes()).append(" minutos\n");
        for (DraftContext.Competency comp : c.competencies()) {
            p.append("\nCompetencia: ").append(comp.name()).append('\n');
            if (!comp.capacities().isEmpty()) {
                p.append("Capacidades: ").append(String.join("; ", comp.capacities())).append('\n');
            }
            if (!comp.performances().isEmpty()) {
                p.append("Desempeños del grado (Programa Curricular de Primaria):\n");
                p.append(comp.performances().stream()
                        .map(d -> "- " + d.description()
                                + (d.page() != null ? " (p. " + d.page() + ")" : "")
                                + (d.requiresConcreteMaterial() ? " [exige material concreto]" : ""))
                        .collect(Collectors.joining("\n"))).append('\n');
            }
        }
        if (!c.criteria().isEmpty()) {
            p.append("\nCriterios de evaluación:\n");
            c.criteria().forEach(cr -> p.append("- ").append(cr).append('\n'));
        }
        if (!c.didacticSteps().isEmpty()) {
            p.append("\nPasos didácticos de la institución para el desarrollo, en orden (usa su número en \"paso\"):\n");
            IntStream.range(0, c.didacticSteps().size()).forEach(i ->
                    p.append(i + 1).append(". ").append(c.didacticSteps().get(i).name()).append('\n'));
        }
        if (c.classroom() != null && !c.classroom().isBlank()) {
            p.append("\nLo que la docente cuenta de su aula y su comunidad:\n").append(c.classroom().trim()).append('\n');
        }
        if (!c.currentDraft().isEmpty()) {
            p.append("\nBorrador vigente de esta sesión. Conserva lo que las indicaciones no piden cambiar:\n");
            c.currentDraft().forEach(a -> p.append("- ").append(a).append('\n'));
        }
        if (!c.indications().isEmpty()) {
            p.append("\nIndicaciones de la docente para este borrador:\n");
            c.indications().forEach(i -> p.append("- ").append(i).append('\n'));
        }
        if (!c.corrections().isEmpty()) {
            p.append("\nCorrige esto del intento anterior:\n");
            c.corrections().forEach(i -> p.append("- ").append(i).append('\n'));
        }
        return p.toString();
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
