package com.allinedu.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Prueba unitaria pura (Q1): sin Spring, sin mocks, sin base de datos. */
class AchievementLevelTest {

    @Test
    void bYCExigenConclusionDescriptivaEnPrimaria() {
        assertTrue(AchievementLevel.B.requiresConclusionInPrimary());
        assertTrue(AchievementLevel.C.requiresConclusionInPrimary());
    }

    @Test
    void adYANoExigenConclusionDescriptiva() {
        assertFalse(AchievementLevel.AD.requiresConclusionInPrimary());
        assertFalse(AchievementLevel.A.requiresConclusionInPrimary());
    }
}
