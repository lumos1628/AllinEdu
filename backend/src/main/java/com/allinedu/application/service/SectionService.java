package com.allinedu.application.service;

import com.allinedu.application.dto.SectionResponse;
import com.allinedu.application.dto.StudentResponse;
import com.allinedu.domain.model.Section;
import com.allinedu.domain.model.User;

import java.util.List;

public interface SectionService {

    List<SectionResponse> getMySections(User user);

    List<StudentResponse> getSectionStudents(User user, Long sectionId);

    /**
     * Devuelve la sección solo si está asignada al docente.
     */
    Section getAssignedSection(User user, Long sectionId);
}