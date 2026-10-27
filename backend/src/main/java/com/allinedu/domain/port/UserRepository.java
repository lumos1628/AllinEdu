package com.allinedu.domain.port;

import com.allinedu.domain.model.User;

import java.util.Optional;

/**
 * PUERTO: el contrato que el negocio necesita para autenticar usuarios.
 * El adaptador construye el User a partir de persona + cuenta + rol.
 */
public interface UserRepository {

    Optional<User> findByEmail(String email);
}