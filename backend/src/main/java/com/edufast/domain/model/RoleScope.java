package com.edufast.domain.model;

/**
 * Alcance de una asignación de rol. INSTITUCION cubre toda la IE;
 * NIVEL_EDUCATIVO limita la asignación a un nivel (por ejemplo, dirección de Primaria).
 */
public enum RoleScope {
    INSTITUCION,
    NIVEL_EDUCATIVO
}
