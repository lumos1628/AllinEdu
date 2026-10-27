package com.allinedu.infrastructure.persistence.adapter;

import com.allinedu.domain.model.Section;
import com.allinedu.domain.port.SectionRepository;
import com.allinedu.infrastructure.persistence.mapper.SectionMapper;
import com.allinedu.infrastructure.persistence.repository.SpringDataAsignacionDocenteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ADAPTADOR del puerto SectionRepository.
 */
@Repository
public class SectionRepositoryAdapter implements SectionRepository {

    private final SpringDataAsignacionDocenteRepository jpa;

    public SectionRepositoryAdapter(SpringDataAsignacionDocenteRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Section> findByTeacherId(Long teacherId) {
        return jpa.findByCuentaIdAndVigenteHastaIsNull(teacherId).stream()
                .map(a -> SectionMapper.toDomain(a.getSeccion()))
                .toList();
    }

    @Override
    public Optional<Section> findAssignedToTeacher(Long teacherId, Long sectionId) {
        return jpa.findByCuentaIdAndSeccionIdAndVigenteHastaIsNull(teacherId, sectionId).stream()
                .findFirst()
                .map(a -> SectionMapper.toDomain(a.getSeccion()));
    }
}