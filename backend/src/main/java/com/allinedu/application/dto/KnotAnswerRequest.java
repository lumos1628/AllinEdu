package com.allinedu.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Respuesta a un nudo: ACEPTADO ("Está bien") o CAMBIO ("Cámbialo"), con lo que quiere en su lugar.
 * remember: guardar ese texto en "Mi aula" para los próximos borradores.
 */
public record KnotAnswerRequest(@NotBlank String answer, @Size(max = 500) String text, boolean remember) {
}
