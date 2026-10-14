package com.edufast.infrastructure.persistence.adapter;

import com.edufast.domain.exception.NotFoundException;
import com.edufast.domain.model.Attendance;
import com.edufast.domain.model.EstadoJornada;
import com.edufast.domain.model.Jornada;
import com.edufast.domain.port.AttendanceRepository;
import com.edufast.infrastructure.persistence.entity.CuentaUsuarioEntity;
import com.edufast.infrastructure.persistence.entity.JornadaAsistenciaEntity;
import com.edufast.infrastructure.persistence.entity.RegistroAsistenciaEntity;
import com.edufast.infrastructure.persistence.entity.SeccionEntity;
import com.edufast.infrastructure.persistence.entity.UbicacionMatriculaEntity;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
            return new Jornada(jornada.getId(), sectionId, date,
                    EstadoJornada.fromPersisted(jornada.getEstado()), registrosJornada);
        });
    }

    @Override
    @Transactional
    public Jornada save(Jornada jornada, Long registeredBy, boolean confirm) {
        SeccionEntity seccion = secciones.getReferenceById(jornada.getSectionId());
        CuentaUsuarioEntity cuenta = cuentas.getReferenceById(registeredBy);

        JornadaAsistenciaEntity jornadaEntity = jornadas
                .findBySeccionIdAndFecha(jornada.getSectionId(), jornada.getDate())
                .orElseGet(() -> new JornadaAsistenciaEntity(null, seccion, jornada.getDate(),
                        EstadoJornada.BORRADOR.name(), cuenta));

        jornadaEntity.setEstado(confirm ? EstadoJornada.CONFIRMADA.name() : EstadoJornada.BORRADOR.name());
        if (confirm) {
            jornadaEntity.setConfirmadaPor(cuenta);
            jornadaEntity.setConfirmadaAt(Instant.now());
        }
        JornadaAsistenciaEntity jornadaGuardada = jornadas.save(jornadaEntity);

        registros.deleteRegistrosDeJornada(jornadaGuardada.getId());

        Map<Long, UbicacionMatriculaEntity> ubicacionesPorEstudiante = ubicaciones
                .findBySeccionIdAndFechaFinIsNull(jornada.getSectionId()).stream()
                .collect(Collectors.toMap(
                        u -> u.getMatricula().getEstudiante().getId(),
                        u -> u,
                        (existente, nuevo) -> existente));

        List<RegistroAsistenciaEntity> nuevos = jornada.getRegistros().stream()
                .map(asistencia -> {
                    UbicacionMatriculaEntity ubicacion = ubicacionesPorEstudiante.get(asistencia.getStudentId());
                    if (ubicacion == null) {
                        throw new NotFoundException("El alumno no está matriculado en esta sección");
                    }
                    String estado = asistencia.getStatus().name();
                    return new RegistroAsistenciaEntity(null, jornadaGuardada, ubicacion, estado);
                })
                .toList();
        registros.saveAll(nuevos);

        return findBySectionAndDate(jornada.getSectionId(), jornada.getDate())
                .orElseThrow(() -> new NotFoundException("No se pudo guardar la asistencia"));
    }
}
