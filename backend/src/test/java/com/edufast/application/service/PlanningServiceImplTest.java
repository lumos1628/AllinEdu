package com.edufast.application.service;

import com.edufast.application.dto.SessionUpdateRequest;
import com.edufast.application.dto.WeekResponse;
import com.edufast.domain.exception.BusinessRuleException;
import com.edufast.domain.exception.NotFoundException;
import com.edufast.domain.model.LearningUnit;
import com.edufast.domain.model.Role;
import com.edufast.domain.model.ScheduleBlock;
import com.edufast.domain.model.Section;
import com.edufast.domain.model.SessionDetail;
import com.edufast.domain.model.SessionEdit;
import com.edufast.domain.model.UnitDetail;
import com.edufast.domain.model.User;
import com.edufast.domain.port.PlanningRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanningServiceImplTest {

    @Mock
    private SectionService sectionService;

    @Mock
    private PlanningRepository planningRepository;

    private PlanningServiceImpl planningService;
    private User teacher;

    // Miércoles 21: la semana debe empezar el lunes 19.
    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 10, 21);

    @BeforeEach
    void setUp() {
        planningService = new PlanningServiceImpl(sectionService, planningRepository);
        teacher = new User(7L, "Docente 4.º B", "tutor.4b@example.test", "hash", Role.DOCENTE);
    }

    @Test
    void laSemanaEmpiezaEnLunesYCadaBloqueLlevaSuFecha() {
        when(sectionService.getAssignedSection(teacher, 8L)).thenReturn(new Section(8L, "B", "4.º de primaria", "PRI-04"));
        when(planningRepository.findSchedule(8L, 7L, WEDNESDAY)).thenReturn(List.of(
                new ScheduleBlock(1, LocalTime.of(8, 0), LocalTime.of(9, 30), "Matemática", true),
                new ScheduleBlock(4, LocalTime.of(11, 30), LocalTime.of(13, 0), "Inglés", false)));
        when(planningRepository.findCurrentOrNextUnit(8L, WEDNESDAY)).thenReturn(Optional.empty());

        WeekResponse week = planningService.getWeek(teacher, 8L, WEDNESDAY);

        assertEquals(LocalDate.of(2026, 10, 19), week.weekStart());
        assertEquals(LocalDate.of(2026, 10, 19), week.blocks().get(0).date());
        assertEquals("8:00", week.blocks().get(0).start());
        assertEquals(LocalDate.of(2026, 10, 22), week.blocks().get(1).date());
        assertFalse(week.blocks().get(1).mine());
        assertNull(week.unit());
    }

    @Test
    void incluyeLaUnidadConSuAvance() {
        when(sectionService.getAssignedSection(teacher, 8L)).thenReturn(new Section(8L, "B", "4.º de primaria", "PRI-04"));
        when(planningRepository.findSchedule(8L, 7L, WEDNESDAY)).thenReturn(List.of());
        when(planningRepository.findCurrentOrNextUnit(8L, WEDNESDAY)).thenReturn(Optional.of(
                new LearningUnit(1L, "Repartimos en partes iguales", "La olla común",
                        LocalDate.of(2026, 10, 20), LocalDate.of(2026, 11, 7), 4, 12)));

        WeekResponse week = planningService.getWeek(teacher, 8L, WEDNESDAY);

        assertEquals("Repartimos en partes iguales", week.unit().title());
        assertEquals(4, week.unit().approvedSessions());
        assertEquals(12, week.unit().totalSessions());
    }

    @Test
    void noMuestraLaSemanaDeUnaSeccionNoAsignada() {
        when(sectionService.getAssignedSection(teacher, 99L))
                .thenThrow(new NotFoundException("Sección no encontrada o no asignada"));

        assertThrows(NotFoundException.class, () -> planningService.getWeek(teacher, 99L, WEDNESDAY));
        verify(planningRepository, never()).findSchedule(any(), any(), any());
    }

    private static SessionDetail session(String status) {
        return new SessionDetail(3L, 1L, "Repartimos en partes iguales", 3, "Comparamos fracciones", "Comparar",
                90, status, false, null, null, List.of("Resuelve problemas de cantidad"), List.of(), List.of(), null,
                List.of(), List.of(), List.of(), List.of());
    }

    @Test
    void noMuestraUnaUnidadDeOtraSeccion() {
        when(planningRepository.isUnitAssignedToTeacher(1L, 7L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> planningService.getUnit(teacher, 1L));
        verify(planningRepository, never()).findUnit(any());
    }

    @Test
    void laUnidadCuentaCuantasSesionesTrabajanCadaCompetencia() {
        when(planningRepository.isUnitAssignedToTeacher(1L, 7L)).thenReturn(true);
        when(planningRepository.findUnit(1L)).thenReturn(Optional.of(new UnitDetail(1L, "Repartimos", null, null,
                null, null,
                List.of(new UnitDetail.Competency(20L, "Resuelve problemas de cantidad", null),
                        new UnitDetail.Competency(21L, "Escribe diversos tipos de textos", null)),
                List.of(new UnitDetail.SessionSummary(1L, 1, "S1", null, "PUBLICADA", List.of("Resuelve problemas de cantidad"), 0),
                        new UnitDetail.SessionSummary(2L, 2, "S2", null, "BORRADOR", List.of("Resuelve problemas de cantidad"), 0)))));

        var unit = planningService.getUnit(teacher, 1L);

        assertEquals(2, unit.competencies().get(0).sessions());
        assertEquals(0, unit.competencies().get(1).sessions());
        assertTrue(unit.sessions().get(0).approved());
        assertFalse(unit.sessions().get(1).approved());
    }

    @Test
    void aprobarUnBorradorLoMarcaComoAprobado() {
        when(planningRepository.findSessionUnitId(3L)).thenReturn(Optional.of(1L));
        when(planningRepository.isUnitAssignedToTeacher(1L, 7L)).thenReturn(true);
        when(planningRepository.findSession(3L)).thenReturn(Optional.of(session("PUBLICADA")));

        var result = planningService.approveSession(teacher, 3L);

        verify(planningRepository).approveSession(3L, 7L);
        assertTrue(result.approved());
    }

    @Test
    void noSePuedeAprobarLaSesionDeOtroDocente() {
        when(planningRepository.findSessionUnitId(3L)).thenReturn(Optional.of(1L));
        when(planningRepository.isUnitAssignedToTeacher(1L, 7L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> planningService.approveSession(teacher, 3L));
        verify(planningRepository, never()).approveSession(any(), any());
    }

    private static SessionUpdateRequest edit(SessionUpdateRequest.Activity... activities) {
        return new SessionUpdateRequest("  Comparamos fracciones  ", " ", 90, "Ficha resuelta", List.of(activities));
    }

    @Test
    void guardaLaEdicionDelDocenteLimpiandoLosTextos() {
        when(planningRepository.findSessionUnitId(3L)).thenReturn(Optional.of(1L));
        when(planningRepository.findSession(3L)).thenReturn(Optional.of(session("PUBLICADA")));
        when(planningRepository.isUnitAssignedToTeacher(1L, 7L)).thenReturn(true);

        planningService.updateSession(teacher, 3L,
                edit(new SessionUpdateRequest.Activity(10L, "INICIO", " ", " Saludo y asistencia ", 5)));

        verify(planningRepository).updateSession(3L, 1L, new SessionEdit("Comparamos fracciones", null, 90,
                "Ficha resuelta", List.of(new SessionEdit.ActivityEdit(10L, "INICIO", null, "Saludo y asistencia", 5))),
                7L);
    }

    @Test
    void rechazaUnaActividadFueraDeLosMomentosDeLaSesion() {
        when(planningRepository.findSessionUnitId(3L)).thenReturn(Optional.of(1L));
        when(planningRepository.isUnitAssignedToTeacher(1L, 7L)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> planningService.updateSession(teacher, 3L,
                edit(new SessionUpdateRequest.Activity(null, "RECREO", null, "Jugar", 10))));
        verify(planningRepository, never()).updateSession(any(), any(), any(), any());
    }

    @Test
    void noSePuedeEditarLaSesionDeOtroDocente() {
        when(planningRepository.findSessionUnitId(3L)).thenReturn(Optional.of(1L));
        when(planningRepository.isUnitAssignedToTeacher(1L, 7L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> planningService.updateSession(teacher, 3L, edit()));
        verify(planningRepository, never()).updateSession(any(), any(), any(), any());
    }
}
