package com.edufast.application.service;

import com.edufast.application.dto.LoginRequest;
import com.edufast.application.dto.LoginResponse;
import com.edufast.domain.exception.UnauthorizedException;
import com.edufast.domain.model.User;
import com.edufast.domain.port.PasswordHasher;
import com.edufast.domain.port.TokenProvider;
import com.edufast.domain.port.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordHasher passwordHasher,
                           TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        if (!passwordHasher.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        String token = tokenProvider.generateToken(user);
        return new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getRoleScope() == null ? null : user.getRoleScope().name(),
                user.getEducationLevelId(),
                user.getSupervisorUserId());
    }
}
