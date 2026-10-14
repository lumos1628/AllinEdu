package com.edufast.application.dto;

import java.time.LocalDate;

/**
 * {@code present} es true para PRESENTE y TARDANZA; {@code status} distingue ambos.
 */
public record AttendanceResponse(
        Long studentId,
        String studentName,
        LocalDate date,
        boolean present,
        String status) {
}
