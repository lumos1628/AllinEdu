package com.allinedu.domain.port;

/**
 * PUERTO: hashea y verifica contraseñas.
 * La implementación (BCrypt) vive en infrastructure/security.
 * Así la capa application no depende de Spring Security.
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);
}
