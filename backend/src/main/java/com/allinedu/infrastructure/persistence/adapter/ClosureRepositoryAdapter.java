package com.allinedu.infrastructure.persistence.adapter;

import com.allinedu.domain.model.AchievementLevel;
import com.allinedu.domain.model.BimesterClosure;
import com.allinedu.domain.port.ClosureRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ADAPTADOR del puerto ClosureRepository, con SQL directo sobre progreso_competencias.
 * El trigger fn_valida_conclusion_descriptiva repite en la base la regla de B y C.
 */
@Repository
public class ClosureRepositoryAdapter implements ClosureRepository {

    private final JdbcClient jdbc;

    public ClosureRepositoryAdapter(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<BimesterClosure> findOpenClosure(Long sectionId) {
        Optional<BimesterClosure.Period> period = jdbc.sql("""
                        SELECT pe.id, pe.nombre, pe.fecha_inicio, pe.fecha_fin
                        FROM public.secciones s
                        JOIN public.periodos_evaluacion pe ON pe.anio_escolar_id = s.anio_escolar_id
                        WHERE s.id = :section AND pe.estado = 'ABIERTO'
                        ORDER BY pe.numero
                        LIMIT 1
                        """)
                .param("section", sectionId)
                .query((rs, i) -> new BimesterClosure.Period(rs.getLong("id"), rs.getString("nombre"),
                        rs.getObject("fecha_inicio", LocalDate.class), rs.getObject("fecha_fin", LocalDate.class)))
                .optional();
        if (period.isEmpty()) {
            return Optional.empty();
        }
        Long periodId = period.get().id();

        record Student(Long enrollmentId, String name, String code) {
        }
        List<Student> students = jdbc.sql("""
                        SELECT m.id, trim(p.nombres || ' ' || p.apellidos) AS nombre, e.codigo_estudiante
                        FROM public.ubicaciones_matricula um
                        JOIN public.matriculas m ON m.id = um.matricula_id
                        JOIN public.estudiantes e ON e.id = m.estudiante_id
                        JOIN public.personas p ON p.id = e.persona_id
                        WHERE um.seccion_id = :section AND um.fecha_fin IS NULL
                        ORDER BY p.apellidos, p.nombres
                        """)
                .param("section", sectionId)
                .query((rs, i) -> new Student(rs.getLong("id"), rs.getString("nombre"), rs.getString("codigo_estudiante")))
                .list();

        // Competencias del periodo: las que ya tienen niveles en la sección y las
        // de las unidades de la sección asociadas a este periodo.
        var competencies = jdbc.sql("""
                        SELECT DISTINCT c.id, c.nombre
                        FROM public.competencias c
                        WHERE c.id IN (
                            SELECT pc.competencia_id
                            FROM public.progreso_competencias pc
                            JOIN public.ubicaciones_matricula um
                                 ON um.matricula_id = pc.matricula_id AND um.seccion_id = :section AND um.fecha_fin IS NULL
                            WHERE pc.periodo_evaluacion_id = :period
                            UNION
                            SELECT ec.competencia_id
                            FROM public.experiencias_competencias ec
                            JOIN public.experiencias_periodos ep ON ep.experiencia_id = ec.experiencia_id
                            JOIN public.experiencias_aprendizaje e ON e.id = ec.experiencia_id
                            JOIN public.destinos_planificacion dp ON dp.planificacion_anual_id = e.planificacion_anual_id
                            WHERE ep.periodo_evaluacion_id = :period AND dp.seccion_id = :section)
                        ORDER BY c.nombre
                        """)
                .param("section", sectionId)
                .param("period", periodId)
                .query((rs, i) -> new BimesterClosure.Competency(rs.getLong("id"), rs.getString("nombre")))
                .list();

        record Row(Long enrollmentId, BimesterClosure.Mark mark) {
        }
        Map<Long, List<BimesterClosure.Mark>> marks = jdbc.sql("""
                        SELECT pc.matricula_id, pc.competencia_id, pc.nivel_logro,
                               pc.conclusion_descriptiva, pc.recomendacion,
                               (SELECT prev.nivel_logro
                                FROM public.progreso_competencias prev
                                JOIN public.periodos_evaluacion pp ON pp.id = prev.periodo_evaluacion_id
                                JOIN public.periodos_evaluacion cur ON cur.id = pc.periodo_evaluacion_id
                                WHERE prev.matricula_id = pc.matricula_id
                                  AND prev.competencia_id = pc.competencia_id
                                  AND pp.anio_escolar_id = cur.anio_escolar_id
                                  AND pp.numero = cur.numero - 1) AS anterior
                        FROM public.progreso_competencias pc
                        JOIN public.ubicaciones_matricula um
                             ON um.matricula_id = pc.matricula_id AND um.seccion_id = :section AND um.fecha_fin IS NULL
                        WHERE pc.periodo_evaluacion_id = :period AND pc.estado = 'EVALUADA'
                        """)
                .param("section", sectionId)
                .param("period", periodId)
                .query((rs, i) -> new Row(rs.getLong("matricula_id"), new BimesterClosure.Mark(
                        rs.getLong("competencia_id"), rs.getString("nivel_logro"), rs.getString("anterior"),
                        rs.getString("conclusion_descriptiva"), rs.getString("recomendacion"))))
                .list().stream()
                .collect(Collectors.groupingBy(Row::enrollmentId,
                        Collectors.mapping(Row::mark, Collectors.toList())));

        var studentMarks = students.stream()
                .map(s -> new BimesterClosure.StudentMarks(s.enrollmentId(), s.name(), s.code(),
                        marks.getOrDefault(s.enrollmentId(), List.of())))
                .toList();
        return Optional.of(new BimesterClosure(period.get(), competencies, studentMarks));
    }

    @Override
    public void saveMark(Long enrollmentId, Long competencyId, Long periodId, AchievementLevel level,
                         String conclusion, String recommendation, Long teacherId) {
        jdbc.sql("""
                        INSERT INTO public.progreso_competencias
                            (matricula_id, competencia_id, periodo_evaluacion_id, estado, nivel_logro,
                             conclusion_descriptiva, recomendacion, registrada_por, registrada_at)
                        VALUES (:enrollment, :competency, :period, 'EVALUADA', :level,
                                :conclusion, :recommendation, :teacher, now())
                        ON CONFLICT (matricula_id, competencia_id, periodo_evaluacion_id) DO UPDATE
                            SET estado = 'EVALUADA',
                                nivel_logro = excluded.nivel_logro,
                                conclusion_descriptiva = excluded.conclusion_descriptiva,
                                recomendacion = excluded.recomendacion,
                                registrada_por = excluded.registrada_por,
                                registrada_at = excluded.registrada_at
                        """)
                .param("enrollment", enrollmentId)
                .param("competency", competencyId)
                .param("period", periodId)
                .param("level", level.name())
                .param("conclusion", conclusion)
                .param("recommendation", recommendation)
                .param("teacher", teacherId)
                .update();
    }
}
