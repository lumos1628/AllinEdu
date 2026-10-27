package com.allinedu.application.service;

import com.allinedu.application.dto.ClassroomRequest;
import com.allinedu.application.dto.ClassroomResponse;
import com.allinedu.application.dto.DraftJobResponse;
import com.allinedu.application.dto.DraftRequest;
import com.allinedu.application.dto.KnotAnswerRequest;
import com.allinedu.application.dto.SessionDetailResponse;
import com.allinedu.domain.exception.BusinessRuleException;
import com.allinedu.domain.exception.GeneratorUnavailableException;
import com.allinedu.domain.exception.NotFoundException;
import com.allinedu.domain.model.DraftContext;
import com.allinedu.domain.model.DraftReview;
import com.allinedu.domain.model.GeneratedDraft;
import com.allinedu.domain.model.Knot;
import com.allinedu.domain.model.SessionDetail;
import com.allinedu.domain.model.User;
import com.allinedu.domain.model.WovenDraft;
import com.allinedu.domain.port.DraftRepository;
import com.allinedu.domain.port.PlanningRepository;
import com.allinedu.domain.port.SessionDraftGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DraftServiceImpl implements DraftService {

    private static final Logger log = LoggerFactory.getLogger(DraftServiceImpl.class);

    static final int CLASSROOM_MAX = 2000;

    private final PlanningRepository planningRepository;
    private final DraftRepository draftRepository;
    private final SessionDraftGenerator generator;
    private final TransactionOperations transactions;
    private final TaskExecutor executor;

    // Preparaciones de unidad en curso o recién terminadas. En memoria a propósito: si el
    // servidor se reinicia, las sesiones ya generadas quedan guardadas y el docente vuelve a pulsar.
    private final Map<Long, Job> jobs = new ConcurrentHashMap<>();

    public DraftServiceImpl(PlanningRepository planningRepository, DraftRepository draftRepository,
                            SessionDraftGenerator generator, TransactionOperations transactions, TaskExecutor executor) {
        this.planningRepository = planningRepository;
        this.draftRepository = draftRepository;
        this.generator = generator;
        this.transactions = transactions;
        this.executor = executor;
    }

    // Sin @Transactional a propósito: la llamada al generador tarda segundos y no
    // debe retener una conexión a la base; solo el guardado va en transacción.
    @Override
    public SessionDetailResponse generateDraft(User user, Long sessionId, DraftRequest request) {
        Long unitId = checkSessionAccess(user, sessionId);
        SessionDetail session = planningRepository.findSession(sessionId).orElseThrow();
        if (session.hasTeacherWriting()) {
            throw new BusinessRuleException(
                    "Ya escribiste parte de esta sesión. Para no perder lo tuyo, cámbiala desde «Editar la sesión».");
        }
        requireGenerator();
        Job job = jobs.get(unitId);
        if (job != null && job.running) {
            throw new BusinessRuleException("Se están preparando las sesiones de esta unidad. Espera a que termine.");
        }

        List<String> indications = new ArrayList<>(session.knots().stream()
                .filter(k -> Knot.CHANGE.equals(k.state()))
                .map(Knot::asIndication)
                .toList());
        if (request != null && request.indications() != null && !request.indications().isBlank()) {
            indications.add(request.indications().trim());
        }

        // Al rehacer, el generador ve el borrador vigente: así cambia lo pedido y conserva lo demás.
        List<String> current = session.activities().stream()
                .map(a -> "[" + a.moment() + "] " + (a.title() != null ? a.title() + ": " : "") + a.instructions()
                        + (a.minutes() != null ? " (" + a.minutes() + " min)" : ""))
                .toList();

        weaveAndSave(sessionId, unitId, user.getId(), indications, current);
        return SessionDetailResponse.from(planningRepository.findSession(sessionId).orElseThrow());
    }

    @Override
    public DraftJobResponse prepareUnit(User user, Long unitId) {
        checkUnitAccess(user, unitId);
        requireGenerator();
        Long teacherId = user.getId();
        synchronized (jobs) {
            Job current = jobs.get(unitId);
            if (current != null && current.running) {
                return current.toResponse(draftRepository.findSessionsWithoutSequence(unitId).size());
            }
            List<Long> pending = draftRepository.findSessionsWithoutSequence(unitId);
            if (pending.isEmpty()) {
                return new DraftJobResponse(false, 0, 0, 0, null, 0);
            }
            Job job = new Job(pending.size());
            jobs.put(unitId, job);
            executor.execute(() -> run(unitId, pending, teacherId, job));
            return job.toResponse(pending.size());
        }
    }

    @Override
    public DraftJobResponse getUnitJob(User user, Long unitId) {
        checkUnitAccess(user, unitId);
        int pending = draftRepository.findSessionsWithoutSequence(unitId).size();
        Job job = jobs.get(unitId);
        return job == null ? new DraftJobResponse(false, 0, 0, 0, null, pending) : job.toResponse(pending);
    }

    @Override
    @Transactional
    public SessionDetailResponse answerKnot(User user, Long sessionId, Long knotId, KnotAnswerRequest request) {
        checkSessionAccess(user, sessionId);
        if (!Knot.ANSWERS.contains(request.answer())) {
            throw new BusinessRuleException("Responde «Está bien» o «Cámbialo».");
        }
        String text = request.text() == null || request.text().isBlank() ? null : request.text().trim();
        if (!draftRepository.answerKnot(sessionId, knotId, request.answer(), text, user.getId())) {
            throw new NotFoundException("Esa pregunta ya no está vigente: el borrador cambió.");
        }
        if (request.remember() && Knot.CHANGE.equals(request.answer()) && text != null) {
            String current = draftRepository.findClassroom(user.getId());
            String merged = current.isBlank() ? text : current.trim() + "\n" + text;
            if (merged.length() > CLASSROOM_MAX) {
                throw new BusinessRuleException("«Mi aula» ya está llena. Resúmela en la unidad para poder agregar esto.");
            }
            draftRepository.saveClassroom(user.getId(), merged);
        }
        return SessionDetailResponse.from(planningRepository.findSession(sessionId).orElseThrow());
    }

    @Override
    @Transactional(readOnly = true)
    public ClassroomResponse getClassroom(User user) {
        return new ClassroomResponse(draftRepository.findClassroom(user.getId()));
    }

    @Override
    @Transactional
    public ClassroomResponse saveClassroom(User user, ClassroomRequest request) {
        String text = request.text().trim();
        if (text.length() > CLASSROOM_MAX) {
            throw new BusinessRuleException("«Mi aula» admite hasta " + CLASSROOM_MAX + " caracteres.");
        }
        draftRepository.saveClassroom(user.getId(), text);
        return new ClassroomResponse(text);
    }

    // Una sesión a la vez y en orden: cada una ve a las anteriores, y el plan gratuito del
    // generador no recibe ráfagas. Si una falla, queda vacía y las demás siguen.
    private void run(Long unitId, List<Long> sessionIds, Long teacherId, Job job) {
        try {
            for (Long sessionId : sessionIds) {
                job.current = sessionId;
                try {
                    // El docente pudo escribirla mientras tanto: entonces no se toca.
                    if (draftRepository.findSessionsWithoutSequence(unitId).contains(sessionId)) {
                        weaveAndSave(sessionId, unitId, teacherId, List.of(), List.of());
                    }
                    job.done++;
                } catch (RuntimeException e) {
                    job.failed++;
                    log.warn("No se pudo preparar la sesión {} de la unidad {}: {}", sessionId, unitId, e.getMessage());
                }
            }
        } finally {
            job.current = null;
            job.running = false;
        }
    }

    // Genera, revisa con las reglas fijas y, si encontraron algo, da un segundo intento con las
    // correcciones. Se queda con el que tenga menos problemas; lo que quede se vuelve nudo.
    private void weaveAndSave(Long sessionId, Long unitId, Long teacherId, List<String> indications,
                              List<String> currentDraft) {
        DraftContext context = draftRepository.findDraftContext(sessionId, teacherId).orElseThrow()
                .withIndications(indications)
                .withCurrentDraft(currentDraft);
        long start = System.nanoTime();

        GeneratedDraft draft = generator.generate(context);
        if (!draft.isUsable()) {
            throw new GeneratorUnavailableException("El borrador llegó incompleto. Intenta de nuevo.");
        }
        int attempts = 1;
        List<String> fixes = DraftReview.corrections(context, draft);
        if (!fixes.isEmpty()) {
            attempts = 2;
            try {
                GeneratedDraft second = generator.generate(context.withCorrections(fixes));
                if (second.isUsable() && DraftReview.corrections(context, second).size() < fixes.size()) {
                    draft = second;
                }
            } catch (GeneratorUnavailableException e) {
                log.info("Sin segundo intento para la sesión {}: se guarda el primero con sus nudos", sessionId);
            }
        }

        WovenDraft woven = WovenDraft.of(context, draft, DraftReview.knotsOf(context, draft), attempts,
                (System.nanoTime() - start) / 1_000_000);
        transactions.executeWithoutResult(status -> draftRepository.saveGeneratedDraft(sessionId, unitId, teacherId, woven));
    }

    private void requireGenerator() {
        if (!generator.isAvailable()) {
            throw new BusinessRuleException("La generación de borradores no está configurada en el servidor.");
        }
    }

    private Long checkSessionAccess(User user, Long sessionId) {
        Long unitId = planningRepository.findSessionUnitId(sessionId)
                .orElseThrow(() -> new NotFoundException("Sesión no encontrada"));
        checkUnitAccess(user, unitId);
        return unitId;
    }

    // Una unidad ajena responde igual que una inexistente: no se revela que existe.
    private void checkUnitAccess(User user, Long unitId) {
        if (unitId == null || !planningRepository.isUnitAssignedToTeacher(unitId, user.getId())) {
            throw new NotFoundException("Unidad no encontrada o no asignada");
        }
    }

    /** Estado de una preparación en lote. Lo escribe un solo hilo; volatile para que se lea al día. */
    private static final class Job {
        final int total;
        volatile int done;
        volatile int failed;
        volatile Long current;
        volatile boolean running = true;

        Job(int total) {
            this.total = total;
        }

        DraftJobResponse toResponse(int pending) {
            return new DraftJobResponse(running, total, done, failed, current, pending);
        }
    }
}
