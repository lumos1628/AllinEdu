package com.edufast.domain.exception;

/**
 * El generador de borradores no respondió o devolvió algo inutilizable.
 */
public class GeneratorUnavailableException extends DomainException {

    public GeneratorUnavailableException(String message) {
        super(message);
    }
}
