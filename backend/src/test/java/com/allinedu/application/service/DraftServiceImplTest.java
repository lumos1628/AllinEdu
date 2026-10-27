package com.allinedu.application.service;

import com.allinedu.application.dto.DraftJobResponse;
import com.allinedu.application.dto.DraftRequest;
import com.allinedu.application.dto.KnotAnswerRequest;
import com.allinedu.domain.exception.BusinessRuleException;
import com.allinedu.domain.exception.GeneratorUnavailableException;
import com.allinedu.domain.exception.NotFoundException;
import com.allinedu.domain.model.DraftContext;
import com.allinedu.domain.model.GeneratedDraft;
import com.allinedu.domain.model.Knot;
import com.allinedu.domain.model.Role;
import com.allinedu.domain.model.SessionDetail;
import com.allinedu.domain.model.User;
import com.allinedu.domain.model.WovenDraft;
import com.allinedu.domain.port.DraftRepository;
import com.allinedu.domain.port.PlanningRepository;
import com.allinedu.domain.port.SessionDraftGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionOperations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DraftServiceImplTest {

    @Mock
    private PlanningRepository planningRepository;

    @Mock
    private DraftRepository draftRepository;

    @Mock
    private SessionDraftGenerator generator;

    private DraftServiceImpl draftService;
    private User teacher;

    private static final DraftContext CONTEXT = new DraftContext("4.º de primaria", List.of("Matemática"),
            "Repartimos", null, null, "¿Cuánto costó?", null, 90, List.of(), List.of(), List.of(), List.of(), "",
            List.of(), List.of(), List.of());

    // Ejecutor síncrono: la preparación en lote termina antes de que el servicio responda.
    @BeforeEach
    void setUp() {
        draftService = new DraftServiceImpl(planningRepository, draftRepository, generator,
                TransactionOperations.withoutTransaction(), Runnable::run);
        teacher = new User(7L, "Docente 4.º B", "tutor.4b@example.test", "hash", Role.DOCENTE);
    }

    private static SessionDetail session(String status, boolean generated, List<SessionDetail.Activity> activities,
                                         List<Knot> knots) {
        return new SessionDetail(5L, 1L, "Repartimos", 5, "¿Cuánto costó?", null, 90, status, generated, null, null,
                List.of(), List.of(), List.of(), null, List.of(), List.of(), List.of(), activities, knots);
    }

    private static GeneratedDraft draft(int... minutes) {
        var activities = java.util.Arrays.stream(minutes)
                .mapToObj(m -> new GeneratedDraft.Activity("DESARROLLO", null, "Resuelven en grupos.", m, null))
                .toList();
        return new GeneratedDraft("Ficha resuelta", activities, List.of(), List.of(), List.of(), "modelo", "v");
    }

    private void accessGranted() {
        when(planningRepository.findSessionUnitId(5L)).thenReturn(Optional.of(1L));
        when(planningRepository.isUnitAssignedToTeacher(1L, 7L)).thenReturn(true);
    }

    @Test
    void guardaElBorradorGeneradoYLoDevuelveSinAprobar() {
        accessGranted();
        when(planningRepository.findSession(5L))
                .thenReturn(Optional.of(session("PUBLICADA", false, List.of(), List.of())))
                .thenReturn(Optional.of(session("BORRADOR", true, List.of(
                        new SessionDetail.Activity(1L, 1, "DESARROLLO", null, "Resuelven en grupos.", 90, true)), List.of())));
        when(generator.isAvailable()).thenReturn(true);
        when(draftRepository.findDraftContext(5L, 7L)).thenReturn(Optional.of(CONTEXT));
        when(generator.generate(CONTEXT)).thenReturn(draft(90));

        var result = draftService.generateDraft(teacher, 5L, null);

        verify(draftRepository).saveGeneratedDraft(eq(5L), eq(1L), eq(7L),
                argThat(w -> w.attempts() == 1 && w.knots().isEmpty()));
        assertFalse(result.approved());
        assertTrue(result.generated());
        assertTrue(result.regenerable());
    }

    @Test
    void noPisaLoQueEscribioElDocente() {
        accessGranted();
        when(planningRepository.findSession(5L)).thenReturn(Optional.of(session("BORRADOR", false, List.of(
                new SessionDetail.Activity(1L, 1, "INICIO", null, "Saludo", 5, false)), List.of())));

        assertThrows(BusinessRuleException.class, () -> draftService.generateDraft(teacher, 5L, null));
        verify(generator, never()).generate(any());
    }

    @Test
    void rehaceElBorradorConLasRespuestasDelDocente() {
        accessGranted();
        var changed = new Knot(3L, Knot.ASSUMPTION, "Supuse que hay un mercado cerca.", null, Knot.CHANGE,
                "Hay una feria los domingos");
        when(planningRepository.findSession(5L)).thenReturn(Optional.of(session("BORRADOR", true, List.of(
                new SessionDetail.Activity(1L, 1, "DESARROLLO", null, "Van al mercado.", 90, true)), List.of(changed))));
        when(generator.isAvailable()).thenReturn(true);
        when(draftRepository.findDraftContext(5L, 7L)).thenReturn(Optional.of(CONTEXT));
        var captor = ArgumentCaptor.forClass(DraftContext.class);
        when(generator.generate(captor.capture())).thenReturn(draft(90));

        draftService.generateDraft(teacher, 5L, new DraftRequest("Que sea más corta"));

        List<String> indications = captor.getValue().indications();
        assertEquals(List.of("[DESARROLLO] Van al mercado. (90 min)"), captor.getValue().currentDraft());
        assertEquals(2, indications.size());
        assertTrue(indications.get(0).contains("Hay una feria los domingos"));
        assertEquals("Que sea más corta", indications.get(1));
    }

    @Test
    void siLasReglasEncuentranAlgoPideUnSegundoIntentoYSeQuedaConElMejor() {
        accessGranted();
        when(planningRepository.findSession(5L)).thenReturn(Optional.of(session("BORRADOR", false, List.of(), List.of())));
        when(generator.isAvailable()).thenReturn(true);
        when(draftRepository.findDraftContext(5L, 7L)).thenReturn(Optional.of(CONTEXT));
        when(generator.generate(any())).thenReturn(draft(60, 40)).thenReturn(draft(50, 40));

        draftService.generateDraft(teacher, 5L, null);

        verify(generator, times(2)).generate(any());
        verify(draftRepository).saveGeneratedDraft(eq(5L), eq(1L), eq(7L),
                argThat(w -> w.attempts() == 2 && w.draft().totalMinutes() == 90 && w.knots().isEmpty()));
    }

    @Test
    void siElSegundoIntentoNoMejoraGuardaElPrimeroConSusNudos() {
        accessGranted();
        when(planningRepository.findSession(5L)).thenReturn(Optional.of(session("BORRADOR", false, List.of(), List.of())));
        when(generator.isAvailable()).thenReturn(true);
        when(draftRepository.findDraftContext(5L, 7L)).thenReturn(Optional.of(CONTEXT));
        when(generator.generate(any())).thenReturn(draft(60, 40)).thenThrow(new GeneratorUnavailableException("ocupado"));

        draftService.generateDraft(teacher, 5L, null);

        var captor = ArgumentCaptor.forClass(WovenDraft.class);
        verify(draftRepository).saveGeneratedDraft(eq(5L), eq(1L), eq(7L), captor.capture());
        assertEquals(100, captor.getValue().draft().totalMinutes());
        assertEquals(Knot.MINUTES, captor.getValue().knots().get(0).type());
    }

    @Test
    void noLlamaAlGeneradorParaUnaUnidadAjena() {
        when(planningRepository.findSessionUnitId(5L)).thenReturn(Optional.of(1L));
        when(planningRepository.isUnitAssignedToTeacher(1L, 7L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> draftService.generateDraft(teacher, 5L, null));
        verify(generator, never()).generate(any());
    }

    @Test
    void sinClaveConfiguradaAvisaEnVezDeFallar() {
        accessGranted();
        when(planningRepository.findSession(5L)).thenReturn(Optional.of(session("BORRADOR", false, List.of(), List.of())));
        when(generator.isAvailable()).thenReturn(false);

        assertThrows(BusinessRuleException.class, () -> draftService.generateDraft(teacher, 5L, null));
    }

    @Test
    void unBorradorConMomentosInvalidosNoSeGuarda() {
        accessGranted();
        when(planningRepository.findSession(5L)).thenReturn(Optional.of(session("BORRADOR", false, List.of(), List.of())));
        when(generator.isAvailable()).thenReturn(true);
        when(draftRepository.findDraftContext(5L, 7L)).thenReturn(Optional.of(CONTEXT));
        when(generator.generate(CONTEXT)).thenReturn(new GeneratedDraft(null,
                List.of(new GeneratedDraft.Activity("RECREO", null, "Jugar", 10, null)),
                List.of(), List.of(), List.of(), "modelo", "v"));

        assertThrows(GeneratorUnavailableException.class, () -> draftService.generateDraft(teacher, 5L, null));
        verify(draftRepository, never()).saveGeneratedDraft(any(), any(), any(), any());
    }

    @Test
    void preparaLaUnidadSesionPorSesionYSigueSiUnaFalla() {
        when(planningRepository.isUnitAssignedToTeacher(1L, 7L)).thenReturn(true);
        when(generator.isAvailable()).thenReturn(true);
        when(draftRepository.findSessionsWithoutSequence(1L)).thenReturn(List.of(5L, 6L));
        when(draftRepository.findDraftContext(any(), eq(7L))).thenReturn(Optional.of(CONTEXT));
        when(generator.generate(any()))
                .thenThrow(new GeneratorUnavailableException("ocupado"))
                .thenReturn(draft(90));

        DraftJobResponse job = draftService.prepareUnit(teacher, 1L);

        assertFalse(job.running());
        assertEquals(2, job.total());
        assertEquals(1, job.done());
        assertEquals(1, job.failed());
        verify(draftRepository, never()).saveGeneratedDraft(eq(5L), any(), any(), any());
        verify(draftRepository).saveGeneratedDraft(eq(6L), eq(1L), eq(7L), any());
    }

    @Test
    void noPreparaUnaUnidadAjena() {
        when(planningRepository.isUnitAssignedToTeacher(1L, 7L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> draftService.prepareUnit(teacher, 1L));
        verify(generator, never()).generate(any());
    }

    @Test
    void cambialoConRecordarloQuedaEnMiAula() {
        accessGranted();
        when(draftRepository.answerKnot(5L, 3L, Knot.CHANGE, "Hay una feria los domingos", 7L)).thenReturn(true);
        when(draftRepository.findClassroom(7L)).thenReturn("Tenemos regletas.");
        when(planningRepository.findSession(5L)).thenReturn(Optional.of(session("BORRADOR", true, List.of(), List.of())));

        draftService.answerKnot(teacher, 5L, 3L, new KnotAnswerRequest(Knot.CHANGE, " Hay una feria los domingos ", true));

        verify(draftRepository).saveClassroom(7L, "Tenemos regletas.\nHay una feria los domingos");
    }

    @Test
    void unaRespuestaQueNoEsEstaBienNiCambialoNoSeGuarda() {
        accessGranted();

        assertThrows(BusinessRuleException.class,
                () -> draftService.answerKnot(teacher, 5L, 3L, new KnotAnswerRequest("QUIZAS", null, false)));
        verify(draftRepository, never()).answerKnot(any(), any(), any(), any(), any());
    }

    @Test
    void unNudoDeUnBorradorAnteriorYaNoSeResponde() {
        accessGranted();
        when(draftRepository.answerKnot(5L, 3L, Knot.ACCEPTED, null, 7L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> draftService.answerKnot(teacher, 5L, 3L, new KnotAnswerRequest(Knot.ACCEPTED, "", false)));
    }
}
