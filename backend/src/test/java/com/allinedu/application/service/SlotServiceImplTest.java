package com.allinedu.application.service;

import com.allinedu.application.dto.PrepareSlotRequest;
import com.allinedu.domain.exception.BusinessRuleException;
import com.allinedu.domain.model.Role;
import com.allinedu.domain.model.Section;
import com.allinedu.domain.model.SessionDetail;
import com.allinedu.domain.model.Slot;
import com.allinedu.domain.model.UnitDetail;
import com.allinedu.domain.model.User;
import com.allinedu.domain.port.PlanningRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotServiceImplTest {

    @Mock
    private SectionService sectionService;

    @Mock
    private PlanningRepository planningRepository;

    private SlotServiceImpl slotService;
    private User teacher;

    // Martes 20 de octubre de 2026; el bloque de Matemática es de los martes (día 2).
    private static final LocalDate TUESDAY = LocalDate.of(2026, 10, 20);
    private static final Slot MATH = new Slot(10L, 8L, 2, LocalTime.of(8, 0), LocalTime.of(9, 30), 1L, "Matemática", true);

    @BeforeEach
    void setUp() {
        slotService = new SlotServiceImpl(sectionService, planningRepository);
        teacher = new User(7L, "Docente 4.º B", "tutor.4b@example.test", "hash", Role.DOCENTE);
        lenient().when(sectionService.getAssignedSection(teacher, 8L))
                .thenReturn(new Section(8L, "B", "4.º de primaria", "PRI-04"));
    }

    private static SessionDetail session(Long id) {
        return new SessionDetail(id, 1L, "Repartimos", 13, "Matemática del martes 20", null, 90, "BORRADOR", false,
                null, null, List.of(), List.of(), List.of(), null, List.of(), List.of(), List.of(), List.of());
    }

    @Test
    void siElBloqueYaTieneSesionEseDiaDevuelveEsa() {
        when(planningRepository.findSlot(10L, 8L, 7L)).thenReturn(Optional.of(MATH));
        when(planningRepository.findSessionAt(10L, TUESDAY)).thenReturn(Optional.of(3L));
        when(planningRepository.findSession(3L)).thenReturn(Optional.of(session(3L)));

        var result = slotService.prepare(teacher, 8L, 10L, new PrepareSlotRequest(TUESDAY, null));

        assertEquals(3L, result.id());
        verify(planningRepository, never()).createScheduledSession(any(), any(), any(), any(), any());
    }

    @Test
    void creaUnaSesionVaciaConTituloDelAreaYElDia() {
        when(planningRepository.findSlot(10L, 8L, 7L)).thenReturn(Optional.of(MATH));
        when(planningRepository.findSessionAt(10L, TUESDAY)).thenReturn(Optional.empty());
        when(planningRepository.findUnitCovering(8L, TUESDAY)).thenReturn(Optional.of(1L));
        when(planningRepository.createScheduledSession(eq(1L), eq(MATH), eq(TUESDAY), any(), eq(7L))).thenReturn(13L);
        when(planningRepository.findSession(13L)).thenReturn(Optional.of(session(13L)));

        slotService.prepare(teacher, 8L, 10L, new PrepareSlotRequest(TUESDAY, null));

        verify(planningRepository).createScheduledSession(1L, MATH, TUESDAY, "Matemática del martes 20", 7L);
    }

    @Test
    void asignaUnaSesionDeLaUnidadQueTodaviaNoTieneFecha() {
        when(planningRepository.findSlot(10L, 8L, 7L)).thenReturn(Optional.of(MATH));
        when(planningRepository.findSessionAt(10L, TUESDAY)).thenReturn(Optional.empty());
        when(planningRepository.findUnitCovering(8L, TUESDAY)).thenReturn(Optional.of(1L));
        when(planningRepository.findUnscheduledSessions(1L, 1L)).thenReturn(List.of(
                new UnitDetail.SessionSummary(7L, 7, "Fracciones en la receta", null, "BORRADOR", List.of(), 0)));
        when(planningRepository.findSession(7L)).thenReturn(Optional.of(session(7L)));

        slotService.prepare(teacher, 8L, 10L, new PrepareSlotRequest(TUESDAY, 7L));

        verify(planningRepository).scheduleSession(7L, 10L, TUESDAY);
    }

    @Test
    void noAsignaUnaSesionQueYaTieneFechaOEsDeOtraArea() {
        when(planningRepository.findSlot(10L, 8L, 7L)).thenReturn(Optional.of(MATH));
        when(planningRepository.findSessionAt(10L, TUESDAY)).thenReturn(Optional.empty());
        when(planningRepository.findUnitCovering(8L, TUESDAY)).thenReturn(Optional.of(1L));
        when(planningRepository.findUnscheduledSessions(1L, 1L)).thenReturn(List.of());

        assertThrows(BusinessRuleException.class,
                () -> slotService.prepare(teacher, 8L, 10L, new PrepareSlotRequest(TUESDAY, 4L)));
        verify(planningRepository, never()).scheduleSession(anyLong(), anyLong(), any());
    }

    @Test
    void noPreparaUnaClaseQueDictaOtroDocente() {
        when(planningRepository.findSlot(10L, 8L, 7L)).thenReturn(Optional.of(
                new Slot(10L, 8L, 2, LocalTime.of(11, 30), LocalTime.of(13, 0), 9L, "Educación Física", false)));

        assertThrows(BusinessRuleException.class,
                () -> slotService.prepare(teacher, 8L, 10L, new PrepareSlotRequest(TUESDAY, null)));
    }

    @Test
    void noPreparaElBloqueEnUnDiaQueNoLeCorresponde() {
        when(planningRepository.findSlot(10L, 8L, 7L)).thenReturn(Optional.of(MATH));

        assertThrows(BusinessRuleException.class,
                () -> slotService.prepare(teacher, 8L, 10L, new PrepareSlotRequest(TUESDAY.plusDays(1), null)));
    }
}
