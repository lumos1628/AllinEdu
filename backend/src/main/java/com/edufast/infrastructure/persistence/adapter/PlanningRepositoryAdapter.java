package com.edufast.infrastructure.persistence.adapter;

import com.edufast.domain.model.LearningUnit;
import com.edufast.domain.model.ScheduleBlock;
import com.edufast.domain.model.SessionDetail;
import com.edufast.domain.model.UnitDetail;
import com.edufast.domain.port.PlanningRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * ADAPTADOR del puerto PlanningRepository. Son lecturas que cruzan varias tablas
 * de planificación sin modificar nada, por eso usa SQL directo y no entidades JPA.
 */
@Repository
public class PlanningRepositoryAdapter implements PlanningRepository {

    private final JdbcClient jdbc;

    public PlanningRepositoryAdapter(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ScheduleBlock> findSchedule(Long sectionId, Long teacherId, LocalDate date) {
        return jdbc.sql("""
                        SELECT h.dia_semana, h.hora_inicio, h.hora_fin, a.nombre AS area,
                               EXISTS (SELECT 1 FROM public.asignaciones_docentes_areas ada
                                       WHERE ada.oferta_curricular_id = h.oferta_curricular_id
                                         AND ada.cuenta_usuario_id = :teacher
                                         AND ada.vigente_hasta IS NULL) AS mine
                        FROM public.horarios_seccion h
                        JOIN public.ofertas_curriculares oc ON oc.id = h.oferta_curricular_id
                        JOIN public.areas_curriculares a ON a.id = oc.area_curricular_id
                        WHERE h.seccion_id = :section
                          AND h.vigente_desde <= :date
                          AND (h.vigente_hasta IS NULL OR h.vigente_hasta >= :date)
                        ORDER BY h.dia_semana, h.hora_inicio
                        """)
                .param("section", sectionId)
                .param("teacher", teacherId)
                .param("date", date)
                .query((rs, i) -> new ScheduleBlock(
                        rs.getInt("dia_semana"),
                        rs.getTime("hora_inicio").toLocalTime(),
                        rs.getTime("hora_fin").toLocalTime(),
                        rs.getString("area"),
                        rs.getBoolean("mine")))
                .list();
    }

    @Override
    public Optional<LearningUnit> findCurrentOrNextUnit(Long sectionId, LocalDate date) {
        return jdbc.sql("""
                        SELECT e.id, e.titulo, e.situacion_significativa, e.fecha_inicio, e.fecha_fin,
                               count(s.id) FILTER (WHERE s.estado = 'PUBLICADA') AS aprobadas,
                               count(s.id) AS total
                        FROM public.experiencias_aprendizaje e
                        JOIN public.destinos_planificacion dp ON dp.planificacion_anual_id = e.planificacion_anual_id
                        LEFT JOIN public.sesiones_planificadas s ON s.experiencia_id = e.id
                        WHERE dp.seccion_id = :section
                          AND e.estado NOT IN ('ARCHIVADA', 'FINALIZADA')
                          AND e.fecha_fin >= :date
                        GROUP BY e.id
                        ORDER BY e.fecha_inicio
                        LIMIT 1
                        """)
                .param("section", sectionId)
                .param("date", date)
                .query((rs, i) -> new LearningUnit(
                        rs.getLong("id"),
                        rs.getString("titulo"),
                        rs.getString("situacion_significativa"),
                        rs.getObject("fecha_inicio", LocalDate.class),
                        rs.getObject("fecha_fin", LocalDate.class),
                        rs.getInt("aprobadas"),
                        rs.getInt("total")))
                .optional();
    }

    @Override
    public boolean isUnitAssignedToTeacher(Long unitId, Long teacherId) {
        return jdbc.sql("""
                        SELECT EXISTS (
                            SELECT 1
                            FROM public.experiencias_aprendizaje e
                            JOIN public.destinos_planificacion dp ON dp.planificacion_anual_id = e.planificacion_anual_id
                            JOIN public.asignaciones_docentes ad ON ad.seccion_id = dp.seccion_id
                            WHERE e.id = :unit
                              AND ad.cuenta_usuario_id = :teacher
                              AND ad.vigente_hasta IS NULL)
                        """)
                .param("unit", unitId)
                .param("teacher", teacherId)
                .query(Boolean.class)
                .single();
    }

    @Override
    public Optional<UnitDetail> findUnit(Long unitId) {
        var competencies = jdbc.sql("""
                        SELECT c.id, c.nombre, ec.proposito_especifico
                        FROM public.experiencias_competencias ec
                        JOIN public.competencias c ON c.id = ec.competencia_id
                        WHERE ec.experiencia_id = :unit
                        ORDER BY c.nombre
                        """)
                .param("unit", unitId)
                .query((rs, i) -> new UnitDetail.Competency(
                        rs.getLong("id"), rs.getString("nombre"), rs.getString("proposito_especifico")))
                .list();

        var sessions = jdbc.sql("""
                        SELECT s.id, s.numero_orden, s.titulo, s.proposito, s.estado,
                               ARRAY(SELECT c.nombre FROM public.sesiones_competencias sc
                                     JOIN public.competencias c ON c.id = sc.competencia_id
                                     WHERE sc.sesion_id = s.id ORDER BY c.nombre) AS competencias,
                               (SELECT count(*) FROM public.actividades_sesion a WHERE a.sesion_id = s.id) AS actividades
                        FROM public.sesiones_planificadas s
                        WHERE s.experiencia_id = :unit AND s.estado <> 'ARCHIVADA'
                        ORDER BY s.numero_orden
                        """)
                .param("unit", unitId)
                .query((rs, i) -> new UnitDetail.SessionSummary(
                        rs.getLong("id"), rs.getInt("numero_orden"), rs.getString("titulo"),
                        rs.getString("proposito"), rs.getString("estado"),
                        strings(rs.getArray("competencias")), rs.getInt("actividades")))
                .list();

        return jdbc.sql("""
                        SELECT id, titulo, situacion_significativa, proposito, fecha_inicio, fecha_fin
                        FROM public.experiencias_aprendizaje WHERE id = :unit
                        """)
                .param("unit", unitId)
                .query((rs, i) -> new UnitDetail(
                        rs.getLong("id"), rs.getString("titulo"), rs.getString("situacion_significativa"),
                        rs.getString("proposito"),
                        rs.getObject("fecha_inicio", LocalDate.class), rs.getObject("fecha_fin", LocalDate.class),
                        competencies, sessions))
                .optional();
    }

    @Override
    public Optional<SessionDetail> findSession(Long sessionId) {
        record Head(Long id, Long unitId, String unitTitle, int number, String title, String purpose,
                    Integer minutes, String status) {
        }
        Optional<Head> head = jdbc.sql("""
                        SELECT s.id, s.experiencia_id, e.titulo AS unidad, s.numero_orden, s.titulo,
                               s.proposito, s.duracion_minutos, s.estado
                        FROM public.sesiones_planificadas s
                        LEFT JOIN public.experiencias_aprendizaje e ON e.id = s.experiencia_id
                        WHERE s.id = :id
                        """)
                .param("id", sessionId)
                .query((rs, i) -> new Head(rs.getLong("id"), (Long) rs.getObject("experiencia_id"),
                        rs.getString("unidad"), rs.getInt("numero_orden"), rs.getString("titulo"),
                        rs.getString("proposito"), (Integer) rs.getObject("duracion_minutos"), rs.getString("estado")))
                .optional();
        if (head.isEmpty()) {
            return Optional.empty();
        }
        Head h = head.get();

        var competencies = jdbc.sql("""
                        SELECT c.nombre FROM public.sesiones_competencias sc
                        JOIN public.competencias c ON c.id = sc.competencia_id
                        WHERE sc.sesion_id = :id ORDER BY c.nombre
                        """)
                .param("id", sessionId).query(String.class).list();

        // Los criterios son de la unidad, para las competencias que trabaja esta sesión.
        var criteria = jdbc.sql("""
                        SELECT ce.descripcion FROM public.criterios_evaluacion ce
                        JOIN public.sesiones_competencias sc
                             ON sc.competencia_id = ce.competencia_id AND sc.sesion_id = :id
                        WHERE ce.experiencia_id = :unit
                        ORDER BY ce.orden
                        """)
                .param("id", sessionId).param("unit", h.unitId()).query(String.class).list();

        var evidences = jdbc.sql("""
                        SELECT descripcion, formato FROM public.evidencias_previstas
                        WHERE sesion_id = :id ORDER BY id
                        """)
                .param("id", sessionId)
                .query((rs, i) -> new SessionDetail.Evidence(rs.getString("descripcion"), rs.getString("formato")))
                .list();

        var dua = jdbc.sql("""
                        SELECT p.nombre, sd.descripcion FROM public.sesiones_dua sd
                        JOIN public.pautas_dua p ON p.id = sd.pauta_dua_id
                        WHERE sd.sesion_id = :id ORDER BY p.orden
                        """)
                .param("id", sessionId)
                .query((rs, i) -> new SessionDetail.Note(rs.getString("nombre"), rs.getString("descripcion")))
                .list();

        var approaches = jdbc.sql("""
                        SELECT e.nombre, se.accion_observable FROM public.sesiones_enfoques se
                        JOIN public.enfoques_transversales e ON e.id = se.enfoque_id
                        WHERE se.sesion_id = :id ORDER BY e.nombre
                        """)
                .param("id", sessionId)
                .query((rs, i) -> new SessionDetail.Note(rs.getString("nombre"), rs.getString("accion_observable")))
                .list();

        var materials = jdbc.sql("""
                        SELECT m.nombre, sm.cantidad, sm.caracter, sm.descripcion
                        FROM public.sesiones_materiales sm
                        JOIN public.materiales_educativos m ON m.id = sm.material_id
                        WHERE sm.sesion_id = :id ORDER BY sm.id
                        """)
                .param("id", sessionId)
                .query((rs, i) -> new SessionDetail.Material(rs.getString("nombre"),
                        (Integer) rs.getObject("cantidad", Integer.class), rs.getString("caracter"),
                        rs.getString("descripcion")))
                .list();

        var activities = jdbc.sql("""
                        SELECT numero_orden, momento, titulo, instrucciones, duracion_minutos
                        FROM public.actividades_sesion WHERE sesion_id = :id ORDER BY numero_orden
                        """)
                .param("id", sessionId)
                .query((rs, i) -> new SessionDetail.Activity(rs.getInt("numero_orden"), rs.getString("momento"),
                        rs.getString("titulo"), rs.getString("instrucciones"),
                        (Integer) rs.getObject("duracion_minutos", Integer.class)))
                .list();

        return Optional.of(new SessionDetail(h.id(), h.unitId(), h.unitTitle(), h.number(), h.title(),
                h.purpose(), h.minutes(), h.status(), competencies, criteria, evidences,
                findInstrument(sessionId, h.unitId()), dua, approaches, materials, activities));
    }

    // El instrumento de la unidad, solo con los criterios de las competencias de esta sesión.
    private SessionDetail.Instrument findInstrument(Long sessionId, Long unitId) {
        record Row(Long instrumentId, String name, String type, Long criterionId, String criterion) {
        }
        var rows = jdbc.sql("""
                        SELECT i.id AS instrumento, i.nombre, i.tipo, ci.id AS criterio, ci.descripcion
                        FROM public.instrumentos_evaluacion i
                        JOIN public.criterios_instrumento ci ON ci.instrumento_id = i.id
                        JOIN public.criterios_evaluacion ce ON ce.id = ci.criterio_id
                        JOIN public.sesiones_competencias sc
                             ON sc.competencia_id = ce.competencia_id AND sc.sesion_id = :id
                        WHERE i.experiencia_id = :unit
                        ORDER BY i.id, ci.orden
                        """)
                .param("id", sessionId).param("unit", unitId)
                .query((rs, i) -> new Row(rs.getLong("instrumento"), rs.getString("nombre"), rs.getString("tipo"),
                        rs.getLong("criterio"), rs.getString("descripcion")))
                .list();
        if (rows.isEmpty()) {
            return null;
        }
        Long first = rows.get(0).instrumentId();
        var criteria = rows.stream()
                .filter(r -> r.instrumentId().equals(first))
                .map(r -> new SessionDetail.InstrumentCriterion(r.criterion(), jdbc.sql("""
                                SELECT codigo, descripcion FROM public.niveles_instrumento
                                WHERE criterio_instrumento_id = :c ORDER BY orden
                                """)
                        .param("c", r.criterionId())
                        .query((rs, i) -> new SessionDetail.Level(rs.getString("codigo"), rs.getString("descripcion")))
                        .list()))
                .toList();
        return new SessionDetail.Instrument(rows.get(0).name(), rows.get(0).type(), criteria);
    }

    @Override
    public void approveSession(Long sessionId) {
        jdbc.sql("UPDATE public.sesiones_planificadas SET estado = 'PUBLICADA' WHERE id = :id AND estado = 'BORRADOR'")
                .param("id", sessionId)
                .update();
    }

    private static List<String> strings(Array array) throws SQLException {
        return array == null ? List.of() : Arrays.asList((String[]) array.getArray());
    }
}
