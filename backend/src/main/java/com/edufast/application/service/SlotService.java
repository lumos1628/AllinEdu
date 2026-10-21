package com.edufast.application.service;

import com.edufast.application.dto.PrepareSlotRequest;
import com.edufast.application.dto.SessionDetailResponse;
import com.edufast.application.dto.SlotOptionsResponse;
import com.edufast.domain.model.User;

import java.time.LocalDate;

public interface SlotService {

    /** Qué se puede usar para la clase de ese bloque en esa fecha. */
    SlotOptionsResponse getOptions(User user, Long sectionId, Long slotId, LocalDate date);

    /**
     * Deja lista la sesión de la clase: asigna una sesión existente de la unidad o
     * crea una vacía. Si el bloque ya tenía sesión ese día, devuelve esa.
     */
    SessionDetailResponse prepare(User user, Long sectionId, Long slotId, PrepareSlotRequest request);
}
