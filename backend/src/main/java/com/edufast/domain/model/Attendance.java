package com.edufast.domain.model;

import java.time.LocalDate;

/**
 * Modelo de dominio PURO: estado de asistencia de un alumno en una fecha.
 */
public class Attendance {

    private final Long studentId;
    private final String studentName;
    private final LocalDate date;
    private final EstadoAsistencia status;

    public Attendance(Long studentId, String studentName, LocalDate date, EstadoAsistencia status) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.date = date;
        this.status = status;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public LocalDate getDate() {
        return date;
    }

    public EstadoAsistencia getStatus() {
        return status;
    }

    /** TARDANZA cuenta como presente para los resúmenes. */
    public boolean isPresent() {
        return status == EstadoAsistencia.PRESENTE || status == EstadoAsistencia.TARDANZA;
    }
}
