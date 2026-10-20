package com.edufast.application.service;

import com.edufast.application.dto.ClosureResponse;
import com.edufast.application.dto.MarkRequest;
import com.edufast.domain.model.User;

public interface ClosureService {

    ClosureResponse getClosure(User user, Long sectionId);

    /** Registra el nivel que eligió el docente y, si corresponde, su conclusión. */
    ClosureResponse saveMark(User user, Long sectionId, MarkRequest request);
}
