package com.allinedu.application.service;

import com.allinedu.application.dto.AttendanceRequest;
import com.allinedu.application.dto.AttendanceResponse;
import com.allinedu.domain.exception.NotFoundException;
import com.allinedu.domain.model.Attendance;
import com.allinedu.domain.model.EstadoAsistencia;
import com.allinedu.domain.model.EstadoJornada;
import com.allinedu.domain.model.Jornada;
import com.allinedu.domain.model.Role;
import com.allinedu.domain.model.Section;
import com.allinedu.domain.model.Student;
import com.allinedu.domain.model.User;
import com.allinedu.domain.port.AttendanceRepository;
import com.allinedu.domain.port.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 9, 14);

    @Mock
    private SectionService sectionService;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private StudentRepository studentRepository;

    private AttendanceServiceImpl attendanceService;

    private User professor;
    private Section section;
    private Student student;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceServiceImpl(sectionService, attendanceRepository, studentRepository);
        professor = new User(1L, "Profesor Demo", "profesor@allinedu.com", "hash", Role.DOCENTE);
        section = new Section(1L, "A", "4.º de primaria", "PRI-04");
        student = new Student(1L, "Ana Torres", "DEMO-PRI-04-A-001");
    }

    @Test
    void tomarAsistenciaCreaJornadaConfirmada() {
        when(sectionService.getAssignedSection(professor, 1L)).thenReturn(section);
        when(studentRepository.findBySectionId(1L)).thenReturn(List.of(student));
        Jornada saved = new Jornada(5L, 1L, FECHA, EstadoJornada.CONFIRMADA,
                List.of(new Attendance(1L, "Ana Torres", FECHA, EstadoAsistencia.PRESENTE)));
        when(attendanceRepository.save(any(Jornada.class), eq(1L), eq(true))).thenReturn(saved);

        List<AttendanceResponse> results = attendanceService.takeAttendance(professor, 1L,
                new AttendanceRequest(FECHA, List.of(new AttendanceRequest.AttendanceEntry(1L, true, null))));

        assertEquals(1, results.size());
        assertTrue(results.get(0).present());
        assertEquals("Ana Torres", results.get(0).studentName());
        verify(attendanceRepository).save(any(Jornada.class), eq(1L), eq(true));
    }

    @Test
    void tomarAsistenciaConTardanzaGuardaElEstadoYCuentaComoPresente() {
        when(sectionService.getAssignedSection(professor, 1L)).thenReturn(section);
        when(studentRepository.findBySectionId(1L)).thenReturn(List.of(student));
        Jornada saved = new Jornada(5L, 1L, FECHA, EstadoJornada.CONFIRMADA,
                List.of(new Attendance(1L, "Ana Torres", FECHA, EstadoAsistencia.TARDANZA)));
        when(attendanceRepository.save(any(Jornada.class), eq(1L), eq(true))).thenReturn(saved);

        List<AttendanceResponse> results = attendanceService.takeAttendance(professor, 1L,
                new AttendanceRequest(FECHA, List.of(new AttendanceRequest.AttendanceEntry(1L, null, "TARDANZA"))));

        verify(attendanceRepository).save(argThat(j ->
                j.getRegistros().get(0).getStatus() == EstadoAsistencia.TARDANZA), eq(1L), eq(true));
        assertEquals("TARDANZA", results.get(0).status());
        assertTrue(results.get(0).present());
    }

    @Test
    void tomarAsistenciaPrefiereStatusSobrePresent() {
        when(sectionService.getAssignedSection(professor, 1L)).thenReturn(section);
        when(studentRepository.findBySectionId(1L)).thenReturn(List.of(student));
        Jornada saved = new Jornada(5L, 1L, FECHA, EstadoJornada.CONFIRMADA,
                List.of(new Attendance(1L, "Ana Torres", FECHA, EstadoAsistencia.AUSENTE)));
        when(attendanceRepository.save(any(Jornada.class), eq(1L), eq(true))).thenReturn(saved);

        attendanceService.takeAttendance(professor, 1L,
                new AttendanceRequest(FECHA, List.of(new AttendanceRequest.AttendanceEntry(1L, true, "AUSENTE"))));

        verify(attendanceRepository).save(argThat(j ->
                j.getRegistros().get(0).getStatus() == EstadoAsistencia.AUSENTE), eq(1L), eq(true));
    }

    @Test
    void tomarAsistenciaDeAlumnoNoMatriculadoLanzaExcepcion() {
        when(sectionService.getAssignedSection(professor, 1L)).thenReturn(section);
        when(studentRepository.findBySectionId(1L)).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> attendanceService.takeAttendance(professor, 1L,
                new AttendanceRequest(FECHA, List.of(new AttendanceRequest.AttendanceEntry(1L, true, null)))));
    }

    @Test
    void tomarAsistenciaConAlumnoDuplicadoUsaLaUltimaEntrada() {
        when(sectionService.getAssignedSection(professor, 1L)).thenReturn(section);
        when(studentRepository.findBySectionId(1L)).thenReturn(List.of(student));
        Jornada saved = new Jornada(5L, 1L, FECHA, EstadoJornada.CONFIRMADA,
                List.of(new Attendance(1L, "Ana Torres", FECHA, EstadoAsistencia.AUSENTE)));
        when(attendanceRepository.save(any(Jornada.class), eq(1L), eq(true))).thenReturn(saved);

        List<AttendanceResponse> results = attendanceService.takeAttendance(professor, 1L,
                new AttendanceRequest(FECHA, List.of(
                        new AttendanceRequest.AttendanceEntry(1L, true, null),
                        new AttendanceRequest.AttendanceEntry(1L, false, null))));

        assertEquals(1, results.size());
        assertFalse(results.get(0).present());
    }

    @Test
    void verAsistenciaFiltraPorSeccionYFecha() {
        when(sectionService.getAssignedSection(professor, 1L)).thenReturn(section);
        Jornada jornada = new Jornada(5L, 1L, FECHA, EstadoJornada.CONFIRMADA,
                List.of(new Attendance(1L, "Ana Torres", FECHA, EstadoAsistencia.PRESENTE)));
        when(attendanceRepository.findBySectionAndDate(1L, FECHA)).thenReturn(Optional.of(jornada));

        List<AttendanceResponse> results = attendanceService.getAttendance(professor, 1L, FECHA);

        assertEquals(1, results.size());
        assertTrue(results.get(0).present());
        assertEquals("Ana Torres", results.get(0).studentName());
    }

    @Test
    void verAsistenciaSinRegistrosDevuelveListaVacia() {
        when(sectionService.getAssignedSection(professor, 1L)).thenReturn(section);
        when(attendanceRepository.findBySectionAndDate(1L, FECHA)).thenReturn(Optional.empty());

        List<AttendanceResponse> results = attendanceService.getAttendance(professor, 1L, FECHA);

        assertTrue(results.isEmpty());
    }

    @Test
    void verAsistenciaDeSeccionNoAsignadaLanzaExcepcion() {
        when(sectionService.getAssignedSection(professor, 99L))
                .thenThrow(new NotFoundException("Sección no encontrada o no asignada"));

        assertThrows(NotFoundException.class, () -> attendanceService.getAttendance(professor, 99L, FECHA));
    }
}
