package com.allinedu.application.dto;

/**
 * Cómo va la preparación de una unidad en lote.
 *
 * @param running          true mientras se generan sesiones
 * @param total            sesiones que entraron en esta preparación
 * @param done             las que ya tienen borrador
 * @param failed           las que no se pudieron generar (quedan vacías para reintentar)
 * @param currentSessionId la que se está generando ahora, o null
 * @param pending          sesiones de la unidad que hoy siguen sin secuencia
 */
public record DraftJobResponse(boolean running, int total, int done, int failed, Long currentSessionId, int pending) {
}
