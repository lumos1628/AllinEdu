package com.allinedu.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Prueba unitaria pura (Q1): sin Spring, sin mocks, sin base de datos. */
class EstadoAsistenciaTest {

    @Test
    void convierteUnValorPersistidoValido() {
        assertEquals(EstadoAsistencia.TARDANZA, EstadoAsistencia.fromPersisted("TARDANZA"));
    }

    @Test
    void fallaConMensajeClaroSiElEstadoEsDesconocido() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> EstadoAsistencia.fromPersisted("FALTO"));
        assertEquals("Estado de asistencia desconocido: FALTO", ex.getMessage());
    }

    @Test
    void fallaSiElEstadoEsNulo() {
        assertThrows(IllegalStateException.class, () -> EstadoAsistencia.fromPersisted(null));
    }
}
