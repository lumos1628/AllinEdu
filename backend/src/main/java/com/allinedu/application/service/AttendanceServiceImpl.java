package com.allinedu.application.service;

import com.allinedu.application.dto.AttendanceRequest;
import com.allinedu.application.dto.AttendanceResponse;
import com.allinedu.domain.exception.NotFoundException;
import com.allinedu.domain.model.Attendance;
import com.allinedu.domain.model.EstadoAsistencia;
import com.allinedu.domain.model.EstadoJornada;
import com.allinedu.domain.model.Jornada;
import com.allinedu.domain.model.Section;
import com.allinedu.domain.model.Student;
import com.allinedu.domain.model.User;
import com.allinedu.domain.port.AttendanceRepository;
import com.allinedu.domain.port.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final SectionService sectionService;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public AttendanceServiceImpl(SectionService sectionService,
                                 AttendanceRepository attendanceRepository,
                                 StudentRepository studentRepository) {
        this.sectionService = sectionService;
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional
    public List<AttendanceResponse> takeAttendance(User user, Long sectionId, AttendanceRequest request) {
        Section section = sectionService.getAssignedSection(user, sectionId);
        Map<Long, Student> matriculados = studentsBySection(sectionId);

        List<Attendance> registros = deduplicate(request.attendance()).stream()
                .map(entry -> toDomain(entry, request.date(), matriculados))
                .toList();

        Jornada jornada = new Jornada(null, section.getId(), request.date(), EstadoJornada.CONFIRMADA, registros);
        Jornada saved = attendanceRepository.save(jornada, user.getId(), true);

        return saved.getRegistros().stream()
                .map(a -> toResponse(a, request.date()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendance(User user, Long sectionId, LocalDate date) {
        sectionService.getAssignedSection(user, sectionId);
        return attendanceRepository.findBySectionAndDate(sectionId, date)
                .map(jornada -> jornada.getRegistros().stream()
                        .map(a -> toResponse(a, date))
                        .toList())
                .orElse(List.of());
    }

    private Map<Long, Student> studentsBySection(Long sectionId) {
        return studentRepository.findBySectionId(sectionId).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));
    }

    private Attendance toDomain(AttendanceRequest.AttendanceEntry entry, LocalDate date,
                                Map<Long, Student> matriculados) {
        Student student = matriculados.get(entry.studentId());
        if (student == null) {
            throw new NotFoundException("El alumno no está matriculado en esta sección");
        }
        return new Attendance(entry.studentId(), student.getName(), date, statusOf(entry));
    }

    // Si llegan ambos campos, manda "status": es el que distingue la tardanza.
    private EstadoAsistencia statusOf(AttendanceRequest.AttendanceEntry entry) {
        if (entry.status() != null) {
            return EstadoAsistencia.valueOf(entry.status());
        }
        return Boolean.TRUE.equals(entry.present()) ? EstadoAsistencia.PRESENTE : EstadoAsistencia.AUSENTE;
    }

    private AttendanceResponse toResponse(Attendance asistencia, LocalDate date) {
        return new AttendanceResponse(asistencia.getStudentId(), asistencia.getStudentName(), date,
                asistencia.isPresent(), asistencia.getStatus().name());
    }

    private List<AttendanceRequest.AttendanceEntry> deduplicate(List<AttendanceRequest.AttendanceEntry> entries) {
        Map<Long, AttendanceRequest.AttendanceEntry> byStudent = new LinkedHashMap<>();
        for (AttendanceRequest.AttendanceEntry entry : entries) {
            byStudent.put(entry.studentId(), entry);
        }
        return List.copyOf(byStudent.values());
    }
}
