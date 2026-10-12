package com.edufast.infrastructure.persistence.adapter;

import com.edufast.domain.exception.NotFoundException;
import com.edufast.domain.model.Attendance;
import com.edufast.domain.model.Jornada;
import com.edufast.domain.port.AttendanceRepository;
import com.edufast.infrastructure.persistence.entity.CuentaUsuarioEntity;
import com.edufast.infrastructure.persistence.entity.JornadaAsistenciaEntity;
import com.edufast.infrastructure.persistence.entity.RegistroAsistenciaEntity;
import com.edufast.infrastructure.persistence.entity.SeccionEntity;
import com.edufast.infrastructure.persistence.mapper.AttendanceMapper;
import com.edufast.infrastructure.persistence.repository.SpringDataCuentaUsuarioRepository;
import com.edufast.infrastructure.persistence.repository.SpringDataJornadaAsistenciaRepository;
import com.edufast.infrastructure.persistence.repository.SpringDataRegistroAsistenciaRepository;
import com.edufast.infrastructure.persistence.repository.SpringDataSeccionRepository;
import com.edufast.infrastructure.persistence.repository.SpringDataUbicacionMatriculaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ADAPTADOR del puerto AttendanceRepository.
 */
@Repository
public class AttendanceRepositoryAdapter implements AttendanceRepository {

    private final SpringDataJornadaAsistenciaRepository jornadas;
    private final SpringDataRegistroAsistenciaRepository registros;
    private final SpringDataUbicacionMatriculaRepository ubicaciones;
    private final SpringDataSeccionRepository secciones;
    private final SpringDataCuentaUsuarioRepository cuentas;

    public AttendanceRepositoryAdapter(SpringDataJornadaAsistenciaRepository jornadas,
                                       SpringDataRegistroAsistenciaRepository registros,
                                       SpringDataUbicacionMatriculaRepository ubicaciones,
                                       SpringDataSeccionRepository secciones,
                                       SpringDataCuentaUsuarioRepository cuentas) {
        this.jornadas = jornadas;
        this.registros = registros;
        this.ubicaciones = ubicaciones;
        this.secciones = secciones;
        this.cuentas = cuentas;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Jornada> findBySectionAndDate(Long sectionId, LocalDate date) {
        return jornadas.findBySeccionIdAndFecha(sectionId, date).map(jornada -> {
            List<Attendance> registrosJornada = registros.findByJornadaId(jornada.getId()).stream()
                    .map(AttendanceMapper::toDomain)
                    .toList();
            return new Jornada(jornada.getId(), sectionId, date, jornada.getEstado(), registrosJornada);
        });
    }

    @Override
    @Transactional
    public Jornada save(Jornada jornada, Long registeredBy, boolean confirm) {
        SeccionEntity seccion = secciones.getReferenceById(jornada.getSectionId());
        CuentaUsuarioEntity cuenta = cuentas.getReferenceById(registeredBy);

        JornadaAsistenciaEntity jornadaEntity = jornadas
                .findBySeccionIdAndFecha(jornada.getSectionId(), jornada.getDate())
                .orElseGet(() -> new JornadaAsistenciaEntity(null, seccion, jornada.getDate(), "BORRADOR", cuenta));

        jornadaEntity.setEstado(confirm ? "CONFIRMADA" : "BORRADOR");
        if (confirm) {
            jornadaEntity.setConfirmadaPor(cuenta);
            jornadaEntity.setConfirmadaAt(Instant.now());
        }
        jornadaEntity = jornadas.save(jornadaEntity);

        registros.deleteRegistrosDeJornada(jornadaEntity.getId());

        for (Attendance asistencia : jornada.getRegistros()) {
            var ubicacion = ubicaciones
                    .findFirstByMatriculaEstudianteIdAndSeccionIdAndFechaFinIsNull(
                            asistencia.getStudentId(), jornada.getSectionId())
                    .orElseThrow(() -> new NotFoundException("El alumno no está matriculado en esta sección"));

            String estado = asistencia.isPresent() ? "PRESENTE" : "AUSENTE";
            registros.save(new RegistroAsistenciaEntity(null, jornadaEntity, ubicacion, estado));
        }

        return findBySectionAndDate(jornada.getSectionId(), jornada.getDate())
                .orElseThrow(() -> new NotFoundException("No se pudo guardar la asistencia"));
    }
}