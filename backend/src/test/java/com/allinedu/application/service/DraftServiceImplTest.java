package com.allinedu.application.service;

import com.allinedu.domain.exception.BusinessRuleException;
import com.allinedu.domain.exception.GeneratorUnavailableException;
import com.allinedu.domain.exception.NotFoundException;
import com.allinedu.domain.model.DraftContext;
import com.allinedu.domain.model.GeneratedDraft;
import com.allinedu.domain.model.Role;
import com.allinedu.domain.model.SessionDetail;
import com.allinedu.domain.model.User;
import com.allinedu.domain.port.PlanningRepository;
import com.allinedu.domain.port.SessionDraftGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionOperations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DraftServiceImplTest {

    @Mock
    private PlanningRepository planningRepository;

    @Mock
    private SessionDraftGenerator generator;

    private DraftServiceImpl draftService;
    private User teacher;

    private static final DraftContext CONTEXT = new DraftContext("4.º de primaria", List.of("Matemática"),
            "Repartimos", null, null, "¿Cuánto costó?", null, 90, List.of(), List.of(), List.of());

    @BeforeEach
    void setUp() {
        draftService = new DraftServiceImpl(planningRepository, generator, TransactionOperations.withoutTransaction());
        teacher = new User(7L, "Docente 4.º B", "tutor.4b@example.test", "hash", Role.DOCENTE);
    }

    private static SessionDetail session(String status, boolean generated, List<SessionDetail.Activity> activities) {
        return new SessionDetail(5L, 1L, "Repartimos", 5, "¿Cuánto costó?", null, 90, status, generated, null, null,
                List.of(), List.of(), List.of(), null, List.of(), List.of(), List.of(), activities);
    }

    private void accessGranted() {
        when(planningRepository.findSessionUnitId(5L)).thenReturn(Optional.of(1L));
        when(planningRepository.isUnitAssignedToTeacher(1L, 7L)).thenReturn(true);
    }

    @Test
    void guardaElBorradorGeneradoYLoDevuelveSinAprobar() {
        accessGranted();
        var draft = new GeneratedDraft("Ficha resuelta",
                List.of(new GeneratedDraft.Activity("INICIO", "Propósito", "Se comunican los criterios.", 15)));
        when(planningRepository.findSession(5L))
                .thenReturn(Optional.of(session("PUBLICADA", false, List.of())))
                .thenReturn(Optional.of(session("BORRADOR", true, List.of(
                        new SessionDetail.Activity(1L, 1, "INICIO", "Propósito", "Se comunican los criterios.", 15, true)))));
        when(generator.isAvailable()).thenReturn(true);
        when(planningRepository.findDraftContext(5L)).thenReturn(Optional.of(CONTEXT));
        when(generator.generate(CONTEXT)).thenReturn(draft);

        var result = draftService.generateDraft(teacher, 5L);

        verify(planningRepository).saveGeneratedDraft(5L, 1L, draft);
        assertFalse(result.approved());
        assertTrue(result.generated());
        assertTrue(result.activities().get(0).generated());
    }

    @Test
    void noReemplazaUnaSecuenciaQueYaExiste() {
        accessGranted();
        when(planningRepository.findSession(5L)).thenReturn(Optional.of(session("BORRADOR", false, List.of(
                new SessionDetail.Activity(1L, 1, "INICIO", null, "Saludo", 5, false)))));

        assertThrows(BusinessRuleException.class, () -> draftService.generateDraft(teacher, 5L));
        verify(generator, never()).generate(any());
    }

    @Test
    void noLlamaAlGeneradorParaUnaUnidadAjena() {
        when(planningRepository.findSessionUnitId(5L)).thenReturn(Optional.of(1L));
        when(planningRepository.isUnitAssignedToTeacher(1L, 7L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> draftService.generateDraft(teacher, 5L));
        verify(generator, never()).generate(any());
    }

    @Test
    void sinClaveConfiguradaAvisaEnVezDeFallar() {
        accessGranted();
        when(planningRepository.findSession(5L)).thenReturn(Optional.of(session("BORRADOR", false, List.of())));
        when(generator.isAvailable()).thenReturn(false);

        assertThrows(BusinessRuleException.class, () -> draftService.generateDraft(teacher, 5L));
    }

    @Test
    void unBorradorConMomentosInvalidosNoSeGuarda() {
        accessGranted();
        when(planningRepository.findSession(5L)).thenReturn(Optional.of(session("BORRADOR", false, List.of())));
        when(generator.isAvailable()).thenReturn(true);
        when(planningRepository.findDraftContext(5L)).thenReturn(Optional.of(CONTEXT));
        when(generator.generate(CONTEXT)).thenReturn(new GeneratedDraft(null,
                List.of(new GeneratedDraft.Activity("RECREO", null, "Jugar", 10))));

        assertThrows(GeneratorUnavailableException.class, () -> draftService.generateDraft(teacher, 5L));
        verify(planningRepository, never()).saveGeneratedDraft(any(), any(), any());
    }
}
