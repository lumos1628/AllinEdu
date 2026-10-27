package com.allinedu.infrastructure.persistence.adapter;

import com.allinedu.domain.model.DraftContext;
import com.allinedu.domain.model.GeneratedDraft;
import com.allinedu.domain.model.Knot;
import com.allinedu.domain.model.WovenDraft;
import com.allinedu.domain.port.DraftRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ADAPTADOR del puerto DraftRepository. Lee el contexto del generador cruzando currículo
 * y planificación, y guarda cada borrador con su registro y sus nudos.
 */
@Repository
public class DraftRepositoryAdapter implements DraftRepository {

    private final JdbcClient jdbc;
    private final JsonMapper json = JsonMapper.builder().build();

    public DraftRepositoryAdapter(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<DraftContext> findDraftContext(Long sessionId, Long teacherId) {
        record Head(Long unitId, Long institutionId, String grade, String unitTitle, String situation,
                    String unitPurpose, int number, String title, String purpose, Integer minutes) {
        }
        // El grado sale de la sección del bloque si la sesión ya tiene fecha; si no, del destino de la unidad.
        Optional<Head> head = jdbc.sql("""
                        SELECT e.id AS unidad, pa.institucion_id, e.titulo AS unidad_titulo, e.situacion_significativa,
                               e.proposito AS unidad_proposito, s.numero_orden, s.titulo, s.proposito, s.duracion_minutos,
                               COALESCE(
                                   (SELECT g.nombre FROM public.horarios_seccion h
                                    JOIN public.secciones sec ON sec.id = h.seccion_id
                                    JOIN public.grados_educativos g ON g.id = sec.grado_id
                                    WHERE h.id = s.horario_id),
                                   (SELECT g.nombre FROM public.destinos_planificacion dp
                                    JOIN public.secciones sec ON sec.id = dp.seccion_id
                                    JOIN public.grados_educativos g ON g.id = sec.grado_id
                                    WHERE dp.planificacion_anual_id = e.planificacion_anual_id
                                    ORDER BY sec.id LIMIT 1)) AS grado
                        FROM public.sesiones_planificadas s
                        JOIN public.experiencias_aprendizaje e ON e.id = s.experiencia_id
                        JOIN public.planificaciones_anuales pa ON pa.id = e.planificacion_anual_id
                        WHERE s.id = :id
                        """)
                .param("id", sessionId)
                .query((rs, i) -> new Head(rs.getLong("unidad"), rs.getLong("institucion_id"), rs.getString("grado"),
                        rs.getString("unidad_titulo"), rs.getString("situacion_significativa"),
                        rs.getString("unidad_proposito"), rs.getInt("numero_orden"), rs.getString("titulo"),
                        rs.getString("proposito"), rs.getObject("duracion_minutos", Integer.class)))
                .optional();
        if (head.isEmpty()) {
            return Optional.empty();
        }
        Head h = head.get();

        record Comp(Long id, String name) {
        }
        List<Comp> comps = jdbc.sql("""
                        SELECT c.id, c.nombre FROM public.sesiones_competencias sc
                        JOIN public.competencias c ON c.id = sc.competencia_id
                        WHERE sc.sesion_id = :id ORDER BY c.nombre
                        """)
                .param("id", sessionId)
                .query((rs, i) -> new Comp(rs.getLong("id"), rs.getString("nombre")))
                .list();

        List<DraftContext.Competency> competencies = comps.stream().map(c -> new DraftContext.Competency(
                c.name(),
                jdbc.sql("SELECT nombre FROM public.capacidades WHERE competencia_id = :c ORDER BY orden")
                        .param("c", c.id()).query(String.class).list(),
                jdbc.sql("""
                                SELECT dg.id, dg.descripcion, dg.fuente_pagina, dg.exige_material_concreto
                                FROM public.desempenos_grado dg
                                JOIN public.grados_educativos g ON g.id = dg.grado_id
                                WHERE dg.competencia_id = :c AND g.nombre = :grade
                                ORDER BY dg.id
                                """)
                        .param("c", c.id()).param("grade", h.grade())
                        .query((rs, i) -> new DraftContext.Performance(rs.getLong("id"), rs.getString("descripcion"),
                                rs.getString("fuente_pagina"), rs.getBoolean("exige_material_concreto")))
                        .list()))
                .toList();

        List<String> areas = jdbc.sql("""
                        SELECT DISTINCT a.nombre FROM public.sesiones_competencias sc
                        JOIN public.competencias_areas ca ON ca.competencia_id = sc.competencia_id
                        JOIN public.areas_curriculares a ON a.id = ca.area_curricular_id
                        WHERE sc.sesion_id = :id ORDER BY a.nombre
                        """)
                .param("id", sessionId).query(String.class).list();

        List<String> criteria = jdbc.sql("""
                        SELECT ce.descripcion FROM public.criterios_evaluacion ce
                        JOIN public.sesiones_competencias sc
                             ON sc.competencia_id = ce.competencia_id AND sc.sesion_id = :id
                        WHERE ce.experiencia_id = :unit ORDER BY ce.orden
                        """)
                .param("id", sessionId).param("unit", h.unitId()).query(String.class).list();

        // Los pasos de la institución para esas competencias; si la institución no tiene los
        // suyos, la plantilla base. Nunca las dos mezcladas.
        List<DraftContext.Step> steps = jdbc.sql("""
                        SELECT pp.id, pp.nombre
                        FROM public.sesiones_competencias sc
                        JOIN LATERAL (
                            SELECT pd.id FROM public.procesos_didacticos pd
                            WHERE pd.competencia_id = sc.competencia_id AND pd.activo
                              AND (pd.institucion_id = :ie OR pd.institucion_id IS NULL)
                            ORDER BY pd.institucion_id NULLS LAST, pd.id
                            LIMIT 1) proceso ON true
                        JOIN public.pasos_proceso_didactico pp ON pp.proceso_id = proceso.id
                        WHERE sc.sesion_id = :id
                        ORDER BY sc.competencia_id, pp.orden
                        """)
                .param("id", sessionId).param("ie", h.institutionId())
                .query((rs, i) -> new DraftContext.Step(rs.getLong("id"), rs.getString("nombre")))
                .list();

        // Las sesiones anteriores, para que el borrador siga el hilo de la unidad.
        List<String> previous = new ArrayList<>(jdbc.sql("""
                        SELECT numero_orden, titulo, proposito FROM public.sesiones_planificadas
                        WHERE experiencia_id = :unit AND numero_orden < :n AND estado <> 'ARCHIVADA'
                        ORDER BY numero_orden DESC
                        LIMIT 3
                        """)
                .param("unit", h.unitId()).param("n", h.number())
                .query((rs, i) -> "Sesión " + rs.getInt("numero_orden") + ": " + rs.getString("titulo")
                        + (rs.getString("proposito") != null ? " (" + rs.getString("proposito") + ")" : ""))
                .list());
        Collections.reverse(previous);

        return Optional.of(new DraftContext(h.grade(), areas, h.unitTitle(), h.situation(), h.unitPurpose(),
                h.title(), h.purpose(), h.minutes() == null ? 90 : h.minutes(), competencies, criteria, steps,
                List.copyOf(previous), findClassroom(teacherId), List.of(), List.of(), List.of()));
    }

    @Override
    public void saveGeneratedDraft(Long sessionId, Long unitId, Long teacherId, WovenDraft woven) {
        GeneratedDraft draft = woven.draft();

        jdbc.sql("DELETE FROM public.actividades_sesion WHERE sesion_id = :id").param("id", sessionId).update();
        for (int i = 0; i < draft.activities().size(); i++) {
            GeneratedDraft.Activity a = draft.activities().get(i);
            jdbc.sql("""
                            INSERT INTO public.actividades_sesion
                                (sesion_id, numero_orden, momento, titulo, instrucciones, duracion_minutos,
                                 paso_proceso_id, origen)
                            VALUES (:session, :order, :moment, :title, :instructions, :minutes, :step, 'GENERADA')
                            """)
                    .param("session", sessionId).param("order", i + 1).param("moment", a.moment())
                    .param("title", cut(a.title(), 180)).param("instructions", a.instructions())
                    .param("minutes", a.minutes()).param("step", woven.stepIds().get(i))
                    .update();
        }

        boolean hasEvidence = jdbc.sql("SELECT EXISTS (SELECT 1 FROM public.evidencias_previstas WHERE sesion_id = :id)")
                .param("id", sessionId).query(Boolean.class).single();
        if (!hasEvidence && draft.evidence() != null && !draft.evidence().isBlank()) {
            jdbc.sql("""
                            INSERT INTO public.evidencias_previstas (experiencia_id, sesion_id, descripcion)
                            VALUES (:unit, :id, :d)
                            """)
                    .param("unit", unitId).param("id", sessionId).param("d", draft.evidence().trim()).update();
        }

        // DUA: lo que escribió el docente se respeta; solo se completan los principios que faltan.
        jdbc.sql("DELETE FROM public.sesiones_dua WHERE sesion_id = :id AND origen = 'GENERADA'")
                .param("id", sessionId).update();
        for (GeneratedDraft.Dua d : draft.dua()) {
            if (d.text() == null || d.text().isBlank() || !GeneratedDraft.DUA_PRINCIPLES.contains(d.principle())) {
                continue;
            }
            jdbc.sql("""
                            INSERT INTO public.sesiones_dua (sesion_id, pauta_dua_id, descripcion, origen)
                            SELECT :id, p.id, :text, 'GENERADA' FROM public.pautas_dua p WHERE p.codigo = :code
                            ON CONFLICT (sesion_id, pauta_dua_id) DO NOTHING
                            """)
                    .param("id", sessionId).param("text", d.text().trim()).param("code", d.principle())
                    .update();
        }

        // Materiales: si el docente ya eligió los suyos, el borrador no agrega otros.
        jdbc.sql("DELETE FROM public.sesiones_materiales WHERE sesion_id = :id AND origen = 'GENERADA'")
                .param("id", sessionId).update();
        boolean teacherMaterials = jdbc.sql("SELECT EXISTS (SELECT 1 FROM public.sesiones_materiales WHERE sesion_id = :id)")
                .param("id", sessionId).query(Boolean.class).single();
        if (!teacherMaterials) {
            for (GeneratedDraft.Material m : draft.materials()) {
                if (m.name() == null || m.name().isBlank()) {
                    continue;
                }
                // OBLIGATORIO solo con un desempeño que lo exija: la base lo verifica (check de la tabla).
                boolean required = m.required() && woven.concreteMaterialSourceId() != null;
                jdbc.sql("""
                                INSERT INTO public.sesiones_materiales
                                    (sesion_id, descripcion, cantidad, caracter, desempeno_id, origen)
                                VALUES (:id, :name, :quantity, :character, :performance, 'GENERADA')
                                """)
                        .param("id", sessionId).param("name", cut(m.name().trim(), 240))
                        .param("quantity", m.quantity() != null && m.quantity() > 0 && m.quantity() < 1000 ? m.quantity() : null)
                        .param("character", required ? "OBLIGATORIO" : "SUGERIDO")
                        .param("performance", required ? woven.concreteMaterialSourceId() : null)
                        .update();
            }
        }

        Long generationId = jdbc.sql("""
                        INSERT INTO public.generaciones_sesion
                            (sesion_id, solicitada_por, modelo, version_prompt, indicaciones, salida, intentos, duracion_ms)
                        VALUES (:id, :teacher, :model, :version, :indications, CAST(:output AS jsonb), :attempts, :ms)
                        RETURNING id
                        """)
                .param("id", sessionId).param("teacher", teacherId)
                .param("model", cut(Objects.requireNonNullElse(draft.model(), "desconocido"), 80))
                .param("version", cut(Objects.requireNonNullElse(draft.promptVersion(), "desconocida"), 20))
                .param("indications", woven.indicationsText())
                .param("output", json.writeValueAsString(draft))
                .param("attempts", woven.attempts())
                .param("ms", (int) Math.min(woven.durationMs(), Integer.MAX_VALUE))
                .query(Long.class).single();

        for (Knot k : woven.knots()) {
            jdbc.sql("""
                            INSERT INTO public.nudos_sesion (sesion_id, generacion_id, tipo, pregunta, fragmento)
                            VALUES (:id, :generation, :type, :question, :fragment)
                            """)
                    .param("id", sessionId).param("generation", generationId).param("type", k.type())
                    .param("question", k.question()).param("fragment", k.fragment())
                    .update();
        }

        jdbc.sql("""
                        UPDATE public.sesiones_planificadas
                        SET origen = 'GENERADA', estado = 'BORRADOR',
                            aprobada_por = NULL, aprobada_at = NULL, editada_por = NULL, editada_at = NULL
                        WHERE id = :id
                        """)
                .param("id", sessionId).update();
    }

    @Override
    public List<Long> findSessionsWithoutSequence(Long unitId) {
        return jdbc.sql("""
                        SELECT s.id FROM public.sesiones_planificadas s
                        WHERE s.experiencia_id = :unit AND s.estado = 'BORRADOR'
                          AND NOT EXISTS (SELECT 1 FROM public.actividades_sesion a WHERE a.sesion_id = s.id)
                        ORDER BY s.numero_orden
                        """)
                .param("unit", unitId).query(Long.class).list();
    }

    @Override
    public List<Knot> findKnots(Long sessionId) {
        return KnotRows.latest(jdbc, sessionId);
    }

    @Override
    public boolean answerKnot(Long sessionId, Long knotId, String state, String answer, Long teacherId) {
        return jdbc.sql("""
                        UPDATE public.nudos_sesion
                        SET estado = :state, respuesta = :answer, resuelto_por = :teacher, resuelto_at = now()
                        WHERE id = :knot AND sesion_id = :id
                          AND generacion_id = (SELECT max(g.id) FROM public.generaciones_sesion g
                                               WHERE g.sesion_id = :id)
                        """)
                .param("state", state).param("answer", answer).param("teacher", teacherId)
                .param("knot", knotId).param("id", sessionId)
                .update() > 0;
    }

    @Override
    public String findClassroom(Long teacherId) {
        return jdbc.sql("SELECT texto FROM public.contextos_aula WHERE cuenta_usuario_id = :t")
                .param("t", teacherId).query(String.class).optional().orElse("");
    }

    @Override
    public void saveClassroom(Long teacherId, String text) {
        jdbc.sql("""
                        INSERT INTO public.contextos_aula (cuenta_usuario_id, texto, updated_at)
                        VALUES (:t, :text, now())
                        ON CONFLICT (cuenta_usuario_id) DO UPDATE SET texto = excluded.texto, updated_at = now()
                        """)
                .param("t", teacherId).param("text", text).update();
    }

    private static String cut(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}
