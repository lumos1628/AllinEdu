package com.allinedu.domain.exception;

/**
 * Credenciales inválidas o sesión no válida. Se traduce a HTTP 401.
 */
public class UnauthorizedException extends DomainException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
