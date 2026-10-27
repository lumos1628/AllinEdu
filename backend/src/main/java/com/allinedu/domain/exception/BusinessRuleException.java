package com.allinedu.domain.exception;

/**
 * Una regla de negocio impide la operación (p. ej. falta la conclusión descriptiva).
 */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
