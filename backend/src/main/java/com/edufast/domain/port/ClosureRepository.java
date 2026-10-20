package com.edufast.domain.port;

import com.edufast.domain.model.AchievementLevel;
import com.edufast.domain.model.BimesterClosure;

import java.util.Optional;

/**
 * PUERTO: lectura y registro de niveles de logro del periodo abierto.
 */
public interface ClosureRepository {

    /** El periodo abierto del año de la sección, con sus estudiantes vigentes. */
    Optional<BimesterClosure> findOpenClosure(Long sectionId);

    void saveMark(Long enrollmentId, Long competencyId, Long periodId, AchievementLevel level,
                  String conclusion, String recommendation, Long teacherId);
}
