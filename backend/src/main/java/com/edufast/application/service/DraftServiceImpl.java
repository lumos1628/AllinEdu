package com.edufast.application.service;

import com.edufast.application.dto.SessionDetailResponse;
import com.edufast.domain.exception.BusinessRuleException;
import com.edufast.domain.exception.GeneratorUnavailableException;
import com.edufast.domain.exception.NotFoundException;
import com.edufast.domain.model.DraftContext;
import com.edufast.domain.model.GeneratedDraft;
import com.edufast.domain.model.SessionDetail;
import com.edufast.domain.model.User;
import com.edufast.domain.port.PlanningRepository;
import com.edufast.domain.port.SessionDraftGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionOperations;

@Service
public class DraftServiceImpl implements DraftService {

    private final PlanningRepository planningRepository;
    private final SessionDraftGenerator generator;
    private final TransactionOperations transactions;

    public DraftServiceImpl(PlanningRepository planningRepository, SessionDraftGenerator generator,
                            TransactionOperations transactions) {
        this.planningRepository = planningRepository;
        this.generator = generator;
        this.transactions = transactions;
    }

    // Sin @Transactional a propósito: la llamada al generador tarda segundos y no
    // debe retener una conexión a la base; solo el guardado va en transacción.
    @Override
    public SessionDetailResponse generateDraft(User user, Long sessionId) {
        Long unitId = planningRepository.findSessionUnitId(sessionId)
                .orElseThrow(() -> new NotFoundException("Sesión no encontrada"));
        if (!planningRepository.isUnitAssignedToTeacher(unitId, user.getId())) {
            throw new NotFoundException("Unidad no encontrada o no asignada");
        }
        SessionDetail session = planningRepository.findSession(sessionId).orElseThrow();
        if (!session.activities().isEmpty()) {
            throw new BusinessRuleException(
                    "Esta sesión ya tiene secuencia. El borrador solo se propone para sesiones vacías, para no reemplazar lo que escribiste.");
        }
        if (!generator.isAvailable()) {
            throw new BusinessRuleException("La generación de borradores no está configurada en el servidor.");
        }

        DraftContext context = planningRepository.findDraftContext(sessionId).orElseThrow();
        GeneratedDraft draft = generator.generate(context);
        if (!draft.isUsable()) {
            throw new GeneratorUnavailableException("El borrador llegó incompleto. Intenta de nuevo.");
        }

        transactions.executeWithoutResult(status -> planningRepository.saveGeneratedDraft(sessionId, unitId, draft));
        return SessionDetailResponse.from(planningRepository.findSession(sessionId).orElseThrow());
    }
}
