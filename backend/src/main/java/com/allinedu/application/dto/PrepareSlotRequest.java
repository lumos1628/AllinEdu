package com.allinedu.application.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** sessionId nulo: crear una sesión nueva para ese bloque y esa fecha. */
public record PrepareSlotRequest(@NotNull LocalDate date, Long sessionId) {
}
