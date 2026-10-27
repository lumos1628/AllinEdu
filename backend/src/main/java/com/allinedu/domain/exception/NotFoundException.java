package com.allinedu.domain.exception;

/**
 * Algo que se pidió no existe (curso, alumno, etc.). Se traduce a HTTP 404.
 */
public class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super(message);
    }
}
