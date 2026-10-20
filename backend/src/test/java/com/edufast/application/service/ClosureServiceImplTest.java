package com.edufast.application.service;

import com.edufast.application.dto.ClosureResponse;
import com.edufast.application.dto.MarkRequest;
import com.edufast.domain.exception.BusinessRuleException;
import com.edufast.domain.exception.NotFoundException;
import com.edufast.domain.model.AchievementLevel;
import com.edufast.domain.model.BimesterClosure;
import com.edufast.domain.model.Role;
import com.edufast.domain.model.Section;
import com.edufast.domain.model.User;
import com.edufast.domain.port.ClosureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClosureServiceImplTest {

    @Mock
    private SectionService sectionService;

    @Mock
    private ClosureRepository closureRepository;

    private ClosureServiceImpl closureService;
    private User teacher;
    private BimesterClosure closure;

    @BeforeEach
    void setUp() {
        closureService = new ClosureServiceImpl(sectionService, closureRepository);
        teacher = new User(7L, "Docente 4.º B", "tutor.4b@example.test", "hash", Role.DOCENTE);
        closure = new BimesterClosure(
                new BimesterClosure.Period(3L, "III bimestre", LocalDate.of(2026, 8, 10), LocalDate.of(2026, 10, 9)),
                List.of(new BimesterClosure.Competency(20L, "Resuelve problemas de cantidad")),
                List.of(new BimesterClosure.StudentMarks(100L, "Ana", "E-1", List.of(
                                new BimesterClosure.Mark(20L, "B", "C", null, null))),
                        new BimesterClosure.StudentMarks(101L, "Luis", "E-2", List.of(
                                new BimesterClosure.Mark(20L, "A", null, null, null)))));
        lenient().when(sectionService.getAssignedSection(teacher, 8L))
                .thenReturn(new Section(8L, "B", "4.º de primaria", "PRI-04"));
    }

    @Test
    void cuentaLosBYCQueTodaviaNoTienenConclusion() {
        when(closureRepository.findOpenClosure(8L)).thenReturn(Optional.of(closure));

        ClosureResponse response = closureService.getClosure(teacher, 8L);

        assertEquals(1, response.pendingConclusions());
        assertTrue(response.students().get(0).marks().get(0).missingConclusion());
        assertFalse(response.students().get(1).marks().get(0).missingConclusion());
    }

    @Test
    void conBoCExigeConclusionYRecomendacion() {
        when(closureRepository.findOpenClosure(8L)).thenReturn(Optional.of(closure));

        var request = new MarkRequest(100L, 20L, "C", "Reconoce las partes con material.", "  ");

        assertThrows(BusinessRuleException.class, () -> closureService.saveMark(teacher, 8L, request));
        verify(closureRepository, never()).saveMark(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void conAoADNoExigeConclusion() {
        when(closureRepository.findOpenClosure(8L)).thenReturn(Optional.of(closure));

        closureService.saveMark(teacher, 8L, new MarkRequest(101L, 20L, "AD", null, null));

        verify(closureRepository).saveMark(101L, 20L, 3L, AchievementLevel.AD, null, null, 7L);
    }

    @Test
    void guardaElNivelQueEligioElDocenteConSuTexto() {
        when(closureRepository.findOpenClosure(8L)).thenReturn(Optional.of(closure));

        closureService.saveMark(teacher, 8L,
                new MarkRequest(100L, 20L, "B", " Compara con tiras. ", "Dos semanas más con material."));

        verify(closureRepository).saveMark(100L, 20L, 3L, AchievementLevel.B,
                "Compara con tiras.", "Dos semanas más con material.", 7L);
    }

    @Test
    void noRegistraNivelesDeEstudiantesDeOtraSeccion() {
        when(closureRepository.findOpenClosure(8L)).thenReturn(Optional.of(closure));

        assertThrows(NotFoundException.class,
                () -> closureService.saveMark(teacher, 8L, new MarkRequest(999L, 20L, "A", null, null)));
        verify(closureRepository, never()).saveMark(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void sinBimestreAbiertoAvisaEnVezDeFallar() {
        when(closureRepository.findOpenClosure(8L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> closureService.getClosure(teacher, 8L));
    }
}
