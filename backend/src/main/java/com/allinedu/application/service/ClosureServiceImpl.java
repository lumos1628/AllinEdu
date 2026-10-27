package com.allinedu.application.service;

import com.allinedu.application.dto.ClosureResponse;
import com.allinedu.application.dto.MarkRequest;
import com.allinedu.domain.exception.BusinessRuleException;
import com.allinedu.domain.exception.NotFoundException;
import com.allinedu.domain.model.AchievementLevel;
import com.allinedu.domain.model.BimesterClosure;
import com.allinedu.domain.model.User;
import com.allinedu.domain.port.ClosureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClosureServiceImpl implements ClosureService {

    private final SectionService sectionService;
    private final ClosureRepository closureRepository;

    public ClosureServiceImpl(SectionService sectionService, ClosureRepository closureRepository) {
        this.sectionService = sectionService;
        this.closureRepository = closureRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ClosureResponse getClosure(User user, Long sectionId) {
        return ClosureResponse.from(openClosure(user, sectionId));
    }

    @Override
    @Transactional
    public ClosureResponse saveMark(User user, Long sectionId, MarkRequest request) {
        BimesterClosure closure = openClosure(user, sectionId);
        if (!closure.hasStudent(request.enrollmentId())) {
            throw new NotFoundException("El estudiante no está en esta sección");
        }
        if (!closure.hasCompetency(request.competencyId())) {
            throw new NotFoundException("La competencia no se evalúa en este periodo");
        }
        AchievementLevel level = AchievementLevel.valueOf(request.level());
        String conclusion = trimToNull(request.conclusion());
        String recommendation = trimToNull(request.recommendation());
        if (level.requiresConclusionInPrimary() && (conclusion == null || recommendation == null)) {
            throw new BusinessRuleException(
                    "Con nivel " + level + " escribe la conclusión descriptiva y la recomendación (RVM N.º 048-2024-MINEDU).");
        }

        closureRepository.saveMark(request.enrollmentId(), request.competencyId(), closure.period().id(),
                level, conclusion, recommendation, user.getId());
        return ClosureResponse.from(closureRepository.findOpenClosure(sectionId).orElseThrow());
    }

    private BimesterClosure openClosure(User user, Long sectionId) {
        sectionService.getAssignedSection(user, sectionId);
        return closureRepository.findOpenClosure(sectionId)
                .orElseThrow(() -> new NotFoundException("No hay un bimestre abierto para esta sección"));
    }

    private static String trimToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
