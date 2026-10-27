package com.allinedu.application.service;

import com.allinedu.application.dto.ClosureResponse;
import com.allinedu.application.dto.MarkRequest;
import com.allinedu.domain.model.User;

public interface ClosureService {

    ClosureResponse getClosure(User user, Long sectionId);

    /** Registra el nivel que eligió el docente y, si corresponde, su conclusión. */
    ClosureResponse saveMark(User user, Long sectionId, MarkRequest request);
}
