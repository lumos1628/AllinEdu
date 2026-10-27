package com.allinedu.domain.model;

/**
 * Nivel de logro de una competencia (CNEB, escala p. 181). Lo elige el docente:
 * el sistema nunca lo calcula (Regla 0b).
 */
public enum AchievementLevel {
    AD, A, B, C;

    /** En primaria, B y C exigen conclusión descriptiva y recomendación (RVM 048-2024). */
    public boolean requiresConclusionInPrimary() {
        return this == B || this == C;
    }
}
