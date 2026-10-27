package com.allinedu.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;

public record AttendanceRequest(
        @NotNull LocalDate date,
        @NotEmpty @Valid List<AttendanceEntry> attendance) {

    /**
     * {@code status} es el formato actual (PRESENTE, AUSENTE, TARDANZA).
     * {@code present} se mantiene para clientes que aún envían solo presente/ausente.
     */
    public record AttendanceEntry(
            @NotNull Long studentId,
            Boolean present,
            @Pattern(regexp = "PRESENTE|AUSENTE|TARDANZA", message = "debe ser PRESENTE, AUSENTE o TARDANZA")
            String status) {

        @AssertTrue(message = "Indica el estado de asistencia del alumno")
        public boolean isStatusGiven() {
            return status != null || present != null;
        }
    }
}
