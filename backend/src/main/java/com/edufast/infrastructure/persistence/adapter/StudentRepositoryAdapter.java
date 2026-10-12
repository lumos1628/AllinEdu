package com.edufast.infrastructure.persistence.adapter;

import com.edufast.domain.model.Student;
import com.edufast.domain.port.StudentRepository;
import com.edufast.infrastructure.persistence.mapper.StudentMapper;
import com.edufast.infrastructure.persistence.repository.SpringDataEstudianteRepository;
import com.edufast.infrastructure.persistence.repository.SpringDataUbicacionMatriculaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ADAPTADOR del puerto StudentRepository.
 */
@Repository
public class StudentRepositoryAdapter implements StudentRepository {

    private final SpringDataEstudianteRepository estudiantes;
    private final SpringDataUbicacionMatriculaRepository ubicaciones;

    public StudentRepositoryAdapter(SpringDataEstudianteRepository estudiantes,
                                    SpringDataUbicacionMatriculaRepository ubicaciones) {
        this.estudiantes = estudiantes;
        this.ubicaciones = ubicaciones;
    }

    @Override
    public Optional<Student> findById(Long id) {
        return estudiantes.findWithPersonaById(id).map(StudentMapper::toDomain);
    }

    @Override
    public List<Student> findBySectionId(Long sectionId) {
        return ubicaciones.findBySeccionIdAndFechaFinIsNull(sectionId).stream()
                .map(u -> StudentMapper.toDomain(u.getMatricula().getEstudiante()))
                .toList();
    }
}