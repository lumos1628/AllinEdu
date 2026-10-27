package com.allinedu.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** "Mi aula": lo que el docente cuenta de su comunidad y sus materiales. Sin datos de estudiantes. */
public record ClassroomRequest(@NotNull @Size(max = 2000) String text) {
}
