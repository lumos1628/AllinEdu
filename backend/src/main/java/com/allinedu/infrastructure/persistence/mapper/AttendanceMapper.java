package com.allinedu.infrastructure.persistence.mapper;

import com.allinedu.domain.model.Attendance;
import com.allinedu.domain.model.EstadoAsistencia;
import com.allinedu.infrastructure.persistence.entity.RegistroAsistenciaEntity;

import java.time.LocalDate;

/**
 * Traductor entre RegistroAsistenciaEntity y el modelo de dominio Attendance.
 */
public final class AttendanceMapper {

    private AttendanceMapper() {
    }

    public static Attendance toDomain(RegistroAsistenciaEntity entity) {
        LocalDate fecha = entity.getJornada().getFecha();
        var estudiante = entity.getUbicacionMatricula().getMatricula().getEstudiante();
        Long studentId = estudiante.getId();
        String studentName = estudiante.getPersona().getNombreCompleto();
        EstadoAsistencia estado = EstadoAsistencia.fromPersisted(entity.getEstado());
        return new Attendance(studentId, studentName, fecha, estado);
    }
}
