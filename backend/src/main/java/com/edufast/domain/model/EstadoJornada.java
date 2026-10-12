package com.edufast.domain.model;

/**
 * Estado de una jornada de asistencia.
 * CONFIRMADA significa que el docente ya cerró la toma de asistencia.
 */
public enum EstadoJornada {
    BORRADOR,
    CONFIRMADA;

    /**
     * Convierte un valor persistido al estado tipado.
     * Falla con un mensaje claro si la base de datos trae un estado desconocido.
     */
    public static EstadoJornada fromPersisted(String value) {
        try {
            return valueOf(value);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalStateException("Estado de jornada desconocido: " + value, ex);
        }
    }
}
