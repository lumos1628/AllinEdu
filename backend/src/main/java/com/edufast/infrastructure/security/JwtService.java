package com.edufast.infrastructure.security;

import com.edufast.domain.model.User;
import com.edufast.domain.port.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * ADAPTADOR del puerto TokenProvider: genera y valida tokens JWT.
 * La capa application solo conoce la interfaz TokenProvider (domain/port).
 */
@Service
public class JwtService implements TokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${edufast.jwt.secret}") String secret,
                      @Value("${edufast.jwt.expiration}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(User user) {
        JwtBuilder builder = Jwts.builder()
                .subject(user.getEmail())
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs));

        if (user.getRoleScope() != null) {
            builder.claim("role_scope", user.getRoleScope().name());
        }
        if (user.getEducationLevelId() != null) {
            builder.claim("education_level_id", user.getEducationLevelId());
        }
        if (user.getSupervisorUserId() != null) {
            builder.claim("supervisor_user_id", user.getSupervisorUserId());
        }

        return builder.signWith(key).compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
