package com.edufast.application.service;

import com.edufast.application.dto.SessionDetailResponse;
import com.edufast.application.dto.UnitDetailResponse;
import com.edufast.application.dto.WeekResponse;
import com.edufast.domain.exception.NotFoundException;
import com.edufast.domain.model.SessionDetail;
import com.edufast.domain.model.User;
import com.edufast.domain.port.PlanningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

@Service
public class PlanningServiceImpl implements PlanningService {

    // Las escuelas del proyecto están en Perú: "hoy" es el de Lima, no el del servidor.
    private static final ZoneId SCHOOL_ZONE = ZoneId.of("America/Lima");

    private final SectionService sectionService;
    private final PlanningRepository planningRepository;

    public PlanningServiceImpl(SectionService sectionService, PlanningRepository planningRepository) {
        this.sectionService = sectionService;
        this.planningRepository = planningRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public WeekResponse getWeek(User user, Long sectionId, LocalDate date) {
        sectionService.getAssignedSection(user, sectionId);
        LocalDate today = date != null ? date : LocalDate.now(SCHOOL_ZONE);
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        var blocks = planningRepository.findSchedule(sectionId, user.getId(), today).stream()
                .map(b -> WeekResponse.Block.from(b, weekStart))
                .toList();
        var unit = planningRepository.findCurrentOrNextUnit(sectionId, today)
                .map(WeekResponse.Unit::from)
                .orElse(null);

        return new WeekResponse(sectionId, weekStart, today, blocks, unit);
    }

    @Override
    @Transactional(readOnly = true)
    public UnitDetailResponse getUnit(User user, Long unitId) {
        checkUnitAccess(user, unitId);
        return planningRepository.findUnit(unitId)
                .map(UnitDetailResponse::from)
                .orElseThrow(() -> new NotFoundException("Unidad no encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public SessionDetailResponse getSession(User user, Long sessionId) {
        return SessionDetailResponse.from(getAccessibleSession(user, sessionId));
    }

    @Override
    @Transactional
    public SessionDetailResponse approveSession(User user, Long sessionId) {
        SessionDetail session = getAccessibleSession(user, sessionId);
        if (session.isDraft()) {
            planningRepository.approveSession(sessionId);
            session = planningRepository.findSession(sessionId).orElseThrow();
        }
        return SessionDetailResponse.from(session);
    }

    private SessionDetail getAccessibleSession(User user, Long sessionId) {
        SessionDetail session = planningRepository.findSession(sessionId)
                .orElseThrow(() -> new NotFoundException("Sesión no encontrada"));
        checkUnitAccess(user, session.unitId());
        return session;
    }

    // Una unidad ajena responde igual que una inexistente: no se revela que existe.
    private void checkUnitAccess(User user, Long unitId) {
        if (unitId == null || !planningRepository.isUnitAssignedToTeacher(unitId, user.getId())) {
            throw new NotFoundException("Unidad no encontrada o no asignada");
        }
    }
}
