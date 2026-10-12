package com.edufast.domain.model;

/**
 * Estado de un registro de asistencia.
 * TARDANZA cuenta como presente para los resúmenes.
 */
public enum EstadoAsistencia {
    PRESENTE,
    AUSENTE,
    TARDANZA;

    /**
     * Convierte un valor persistido al estado tipado.
     * Falla con un mensaje claro si la base de datos trae un estado desconocido.
     */
    public static EstadoAsistencia fromPersisted(String value) {
        try {
            return valueOf(value);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalStateException("Estado de asistencia desconocido: " + value, ex);
        }
    }
}
