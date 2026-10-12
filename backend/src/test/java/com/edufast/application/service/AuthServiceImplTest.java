package com.edufast.application.service;

import com.edufast.application.dto.LoginRequest;
import com.edufast.application.dto.LoginResponse;
import com.edufast.domain.exception.UnauthorizedException;
import com.edufast.domain.model.Role;
import com.edufast.domain.model.RoleScope;
import com.edufast.domain.model.User;
import com.edufast.domain.port.PasswordHasher;
import com.edufast.domain.port.TokenProvider;
import com.edufast.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenProvider tokenProvider;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordHasher, tokenProvider);
    }

    @Test
    void loginConCredencialesValidasDevuelveToken() {
        User user = new User(1L, "Profesor Demo", "profesor@edufast.com", "hash", Role.DOCENTE);
        when(userRepository.findByEmail("profesor@edufast.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("123456", "hash")).thenReturn(true);
        when(tokenProvider.generateToken(user)).thenReturn("token-de-prueba");

        LoginResponse response = authService.login(
                new LoginRequest("profesor@edufast.com", "123456"));

        assertEquals("token-de-prueba", response.token());
        assertEquals("Profesor Demo", response.name());
        assertEquals("DOCENTE", response.role());
    }

    @Test
    void loginDeDirectorIncluyeSuAlcanceInstitucional() {
        User user = new User(
                2L,
                "Directora Demo",
                "director@edufast.test",
                "hash",
                Role.DIRECTOR,
                RoleScope.NIVEL_EDUCATIVO,
                (short) 1,
                null);
        when(userRepository.findByEmail("director@edufast.test")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("clave-local", "hash")).thenReturn(true);
        when(tokenProvider.generateToken(user)).thenReturn("token-directora");

        LoginResponse response = authService.login(
                new LoginRequest("director@edufast.test", "clave-local"));

        assertEquals("DIRECTOR", response.role());
        assertEquals("NIVEL_EDUCATIVO", response.roleScope());
        assertEquals((short) 1, response.educationLevelId());
    }

    @Test
    void loginDeSecretariaIncluyeSuDirectorResponsable() {
        User user = new User(
                3L,
                "Secretaria Demo",
                "secretaria@edufast.test",
                "hash",
                Role.SECRETARIA,
                null,
                null,
                2L);
        when(userRepository.findByEmail("secretaria@edufast.test")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("clave-local", "hash")).thenReturn(true);
        when(tokenProvider.generateToken(user)).thenReturn("token-secretaria");

        LoginResponse response = authService.login(
                new LoginRequest("secretaria@edufast.test", "clave-local"));

        assertEquals("SECRETARIA", response.role());
        assertEquals(2L, response.supervisorUserId());
    }

    @Test
    void loginConPasswordIncorrectoLanzaExcepcion() {
        User user = new User(1L, "Profesor Demo", "profesor@edufast.com", "hash", Role.DOCENTE);
        when(userRepository.findByEmail("profesor@edufast.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("password-incorrecta", "hash")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () ->
                authService.login(new LoginRequest("profesor@edufast.com", "password-incorrecta")));
    }

    @Test
    void loginConEmailInexistenteLanzaExcepcion() {
        when(userRepository.findByEmail("nadie@edufast.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () ->
                authService.login(new LoginRequest("nadie@edufast.com", "123456")));
    }
}
