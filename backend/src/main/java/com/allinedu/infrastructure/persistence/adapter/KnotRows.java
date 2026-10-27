package com.allinedu.infrastructure.persistence.adapter;

import com.allinedu.domain.model.Knot;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;

/** Lectura de los nudos vigentes de una sesión: los del último borrador generado. */
final class KnotRows {

    private KnotRows() {
    }

    static List<Knot> latest(JdbcClient jdbc, Long sessionId) {
        return jdbc.sql("""
                        SELECT n.id, n.tipo, n.pregunta, n.fragmento, n.estado, n.respuesta
                        FROM public.nudos_sesion n
                        WHERE n.sesion_id = :id
                          AND n.generacion_id = (SELECT max(g.id) FROM public.generaciones_sesion g
                                                 WHERE g.sesion_id = :id)
                        ORDER BY n.id
                        """)
                .param("id", sessionId)
                .query((rs, i) -> new Knot(rs.getLong("id"), rs.getString("tipo"), rs.getString("pregunta"),
                        rs.getString("fragmento"), rs.getString("estado"), rs.getString("respuesta")))
                .list();
    }
}
