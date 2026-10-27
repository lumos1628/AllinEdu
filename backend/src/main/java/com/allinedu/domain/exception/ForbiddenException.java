package com.allinedu.domain.exception;

/**
 * El usuario existe pero no tiene permiso sobre el recurso. Se traduce a HTTP 403.
 */
public class ForbiddenException extends DomainException {

    public ForbiddenException(String message) {
        super(message);
    }
}
