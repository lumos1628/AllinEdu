package com.allinedu.domain.exception;

/**
 * Excepción base de todo error de negocio.
 * Vive en el dominio: NO conoce HTTP ni códigos de estado.
 * La traducción a HTTP ocurre en infrastructure (GlobalExceptionHandler).
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
