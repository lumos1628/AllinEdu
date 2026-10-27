package com.allinedu.application.service;

import com.allinedu.application.dto.SectionResponse;
import com.allinedu.application.dto.StudentResponse;
import com.allinedu.domain.exception.NotFoundException;
import com.allinedu.domain.model.Section;
import com.allinedu.domain.model.User;
import com.allinedu.domain.port.SectionRepository;
import com.allinedu.domain.port.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final StudentRepository studentRepository;

    public SectionServiceImpl(SectionRepository sectionRepository, StudentRepository studentRepository) {
        this.sectionRepository = sectionRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionResponse> getMySections(User user) {
        return sectionRepository.findByTeacherId(user.getId()).stream()
                .map(SectionResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getSectionStudents(User user, Long sectionId) {
        getAssignedSection(user, sectionId);
        return studentRepository.findBySectionId(sectionId).stream()
                .map(StudentResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Section getAssignedSection(User user, Long sectionId) {
        return sectionRepository.findAssignedToTeacher(user.getId(), sectionId)
                .orElseThrow(() -> new NotFoundException("Sección no encontrada o no asignada"));
    }
}