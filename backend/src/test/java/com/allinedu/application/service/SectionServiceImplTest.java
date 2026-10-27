package com.allinedu.application.service;

import com.allinedu.application.dto.SectionResponse;
import com.allinedu.application.dto.StudentResponse;
import com.allinedu.domain.exception.NotFoundException;
import com.allinedu.domain.model.Role;
import com.allinedu.domain.model.Section;
import com.allinedu.domain.model.Student;
import com.allinedu.domain.model.User;
import com.allinedu.domain.port.SectionRepository;
import com.allinedu.domain.port.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SectionServiceImplTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private StudentRepository studentRepository;

    private SectionServiceImpl sectionService;

    private User professor;
    private Section section;

    @BeforeEach
    void setUp() {
        sectionService = new SectionServiceImpl(sectionRepository, studentRepository);
        professor = new User(1L, "Profesor Demo", "profesor@allinedu.com", "hash", Role.DOCENTE);
        section = new Section(1L, "A", "4.º de primaria", "PRI-04");
    }

    @Test
    void getMySectionsDevuelveLasSeccionesAsignadas() {
        when(sectionRepository.findByTeacherId(1L)).thenReturn(List.of(section));

        List<SectionResponse> results = sectionService.getMySections(professor);

        assertEquals(1, results.size());
        assertEquals("4.º de primaria - A", results.get(0).descripcion());
    }

    @Test
    void getSectionStudentsSoloSiLaSeccionEstaAsignada() {
        when(sectionRepository.findAssignedToTeacher(1L, 1L)).thenReturn(Optional.of(section));
        when(studentRepository.findBySectionId(1L))
                .thenReturn(List.of(new Student(1L, "Ana Torres", "DEMO-PRI-04-A-001")));

        List<StudentResponse> results = sectionService.getSectionStudents(professor, 1L);

        assertEquals(1, results.size());
        assertEquals("Ana Torres", results.get(0).name());
    }

    @Test
    void getSectionStudentsDeSeccionNoAsignadaLanzaExcepcion() {
        when(sectionRepository.findAssignedToTeacher(1L, 99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> sectionService.getSectionStudents(professor, 99L));
    }

    @Test
    void getAssignedSectionDeSeccionInexistenteLanzaExcepcion() {
        when(sectionRepository.findAssignedToTeacher(1L, 99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> sectionService.getAssignedSection(professor, 99L));
    }
}
