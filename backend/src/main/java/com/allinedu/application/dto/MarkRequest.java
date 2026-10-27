package com.allinedu.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record MarkRequest(
        @NotNull Long enrollmentId,
        @NotNull Long competencyId,
        @NotNull @Pattern(regexp = "AD|A|B|C", message = "El nivel debe ser AD, A, B o C") String level,
        String conclusion,
        String recommendation) {
}
