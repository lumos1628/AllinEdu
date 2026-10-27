package com.allinedu.application.service;

import com.allinedu.application.dto.PrepareSlotRequest;
import com.allinedu.application.dto.SessionDetailResponse;
import com.allinedu.application.dto.SlotOptionsResponse;
import com.allinedu.application.dto.UnitDetailResponse;
import com.allinedu.domain.exception.BusinessRuleException;
import com.allinedu.domain.exception.NotFoundException;
import com.allinedu.domain.model.Slot;
import com.allinedu.domain.model.UnitDetail;
import com.allinedu.domain.model.User;
import com.allinedu.domain.port.PlanningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
public class SlotServiceImpl implements SlotService {

    private static final Locale ES = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("H:mm");

    private final SectionService sectionService;
    private final PlanningRepository planningRepository;

    public SlotServiceImpl(SectionService sectionService, PlanningRepository planningRepository) {
        this.sectionService = sectionService;
        this.planningRepository = planningRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SlotOptionsResponse getOptions(User user, Long sectionId, Long slotId, LocalDate date) {
        Slot slot = ownSlotOn(user, sectionId, slotId, date);
        Long existing = planningRepository.findSessionAt(slotId, date).orElse(null);
        Long unitId = planningRepository.findUnitCovering(sectionId, date).orElse(null);

        String unitTitle = null;
        var candidates = java.util.List.<UnitDetailResponse.Session>of();
        if (unitId != null) {
            unitTitle = planningRepository.findUnit(unitId).map(UnitDetail::title).orElse(null);
            candidates = planningRepository.findUnscheduledSessions(unitId, slot.areaId()).stream()
                    .map(s -> new UnitDetailResponse.Session(s.id(), s.number(), s.title(), s.purpose(),
                            "PUBLICADA".equals(s.status()), s.competencies(), s.activities(), s.openKnots()))
                    .toList();
        }
        return new SlotOptionsResponse(slot.id(), date, slot.area(), slot.start().format(HOUR),
                slot.end().format(HOUR), unitId, unitTitle, existing, candidates);
    }

    @Override
    @Transactional
    public SessionDetailResponse prepare(User user, Long sectionId, Long slotId, PrepareSlotRequest request) {
        LocalDate date = request.date();
        Slot slot = ownSlotOn(user, sectionId, slotId, date);

        var existing = planningRepository.findSessionAt(slotId, date);
        if (existing.isPresent()) {
            return session(existing.get());
        }
        Long unitId = planningRepository.findUnitCovering(sectionId, date)
                .orElseThrow(() -> new BusinessRuleException(
                        "No hay una unidad que cubra esa fecha. Crea o extiende la unidad primero."));

        Long sessionId;
        if (request.sessionId() != null) {
            boolean candidate = planningRepository.findUnscheduledSessions(unitId, slot.areaId()).stream()
                    .anyMatch(s -> s.id().equals(request.sessionId()));
            if (!candidate) {
                throw new BusinessRuleException("Esa sesión ya tiene fecha o no es de esta área y unidad.");
            }
            planningRepository.scheduleSession(request.sessionId(), slotId, date);
            sessionId = request.sessionId();
        } else {
            String day = date.getDayOfWeek().getDisplayName(TextStyle.FULL, ES);
            String title = slot.area() + " del " + day + " " + date.getDayOfMonth();
            sessionId = planningRepository.createScheduledSession(unitId, slot, date, title, user.getId());
        }
        return session(sessionId);
    }

    private SessionDetailResponse session(Long sessionId) {
        return SessionDetailResponse.from(planningRepository.findSession(sessionId).orElseThrow());
    }

    // Solo los bloques que dicta el propio docente, y solo en su día de la semana.
    private Slot ownSlotOn(User user, Long sectionId, Long slotId, LocalDate date) {
        sectionService.getAssignedSection(user, sectionId);
        Slot slot = planningRepository.findSlot(slotId, sectionId, user.getId())
                .orElseThrow(() -> new NotFoundException("Bloque del horario no encontrado"));
        if (!slot.taughtByTeacher()) {
            throw new BusinessRuleException("Esta clase la dicta otro docente.");
        }
        if (!slot.fallsOn(date)) {
            throw new BusinessRuleException("Ese bloque no se dicta en la fecha indicada.");
        }
        return slot;
    }
}
