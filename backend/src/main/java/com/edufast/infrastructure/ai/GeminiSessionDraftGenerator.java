package com.edufast.infrastructure.ai;

import com.edufast.domain.exception.GeneratorUnavailableException;
import com.edufast.domain.model.DraftContext;
import com.edufast.domain.model.GeneratedDraft;
import com.edufast.domain.port.SessionDraftGenerator;
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

/**
 * ADAPTADOR del puerto SessionDraftGenerator sobre la API de Gemini.
 * Prueba los modelos configurados en orden: el plan gratuito a veces responde
 * 503 por demanda y otro modelo sí atiende.
 */
@Component
public class GeminiSessionDraftGenerator implements SessionDraftGenerator {

    private static final Logger log = LoggerFactory.getLogger(GeminiSessionDraftGenerator.class);

    private static final String SYSTEM = """
            Eres un asistente que propone BORRADORES de sesiones de aprendizaje para docentes de \
            primaria de escuelas públicas del Perú, según el Currículo Nacional de la Educación Básica (CNEB).
            Reglas:
            - Escribe en español claro, en tercera persona y sin adornos ("Los estudiantes...", "La docente...").
            - Organiza la sesión en tres momentos: INICIO, DESARROLLO y CIERRE.
            - En el INICIO incluye siempre comunicar el propósito y los criterios de evaluación a los estudiantes.
            - Si la institución tiene pasos didácticos para el área, el DESARROLLO sigue esos pasos en ese orden.
            - Si algún desempeño exige material concreto, úsalo en el desarrollo.
            - Los minutos de todas las actividades deben sumar exactamente la duración de la sesión.
            - Usa solo la información dada. No inventes nombres de estudiantes, datos personales, \
            diagnósticos ni calificaciones: la evaluación y las decisiones las toma la docente.
            - La evidencia de aprendizaje es lo que producen o hacen los estudiantes y permite ver los criterios.
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
                                            "minutos", Map.of("type", "INTEGER")),
                                    "required", List.of("momento", "titulo", "instrucciones", "minutos")))),
            "required", List.of("evidencia", "actividades"));

    private final String apiKey;
    private final List<String> models;
    private final RestClient client;
    private final JsonMapper json = JsonMapper.builder().build();

    public GeminiSessionDraftGenerator(
            @Value("${edufast.ai.gemini.api-key:}") String apiKey,
            @Value("${edufast.ai.gemini.models:gemini-3.1-flash-lite,gemini-flash-lite-latest}") String models,
            @Value("${edufast.ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl) {
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
                        "temperature", 0.6,
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
                GeneratedDraft draft = parse(response);
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

    private GeneratedDraft parse(String response) {
        JsonNode parts = json.readTree(response).path("candidates").path(0).path("content").path("parts");
        String text = parts.path(parts.size() - 1).path("text").asString();
        JsonNode root = json.readTree(text);
        List<GeneratedDraft.Activity> activities = new ArrayList<>();
        for (JsonNode a : root.path("actividades")) {
            int minutes = a.path("minutos").asInt(0);
            activities.add(new GeneratedDraft.Activity(
                    a.path("momento").asString(),
                    blankToNull(a.path("titulo").asString()),
                    a.path("instrucciones").asString(),
                    minutes > 0 ? minutes : null));
        }
        return new GeneratedDraft(blankToNull(root.path("evidencia").asString()), activities);
    }

    static String prompt(DraftContext c) {
        StringBuilder p = new StringBuilder();
        p.append("Propón la secuencia didáctica de esta sesión.\n\n");
        p.append("Grado: ").append(c.grade()).append('\n');
        if (!c.areas().isEmpty()) p.append("Área: ").append(String.join(", ", c.areas())).append('\n');
        p.append("Unidad: ").append(c.unitTitle()).append('\n');
        if (c.unitSituation() != null) p.append("Situación significativa: ").append(c.unitSituation()).append('\n');
        if (c.unitPurpose() != null) p.append("Propósito de la unidad: ").append(c.unitPurpose()).append('\n');
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
            p.append("\nPasos didácticos de la institución para el desarrollo, en orden:\n");
            c.didacticSteps().forEach(s -> p.append("- ").append(s).append('\n'));
        }
        return p.toString();
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
