package com.allinedu.infrastructure.persistence.adapter;

import com.allinedu.domain.model.Role;
import com.allinedu.domain.model.RoleScope;
import com.allinedu.domain.model.User;
import com.allinedu.domain.port.UserRepository;
import com.allinedu.infrastructure.persistence.entity.AsignacionRolEntity;
import com.allinedu.infrastructure.persistence.mapper.CuentaUsuarioMapper;
import com.allinedu.infrastructure.persistence.repository.SpringDataAsignacionRolRepository;
import com.allinedu.infrastructure.persistence.repository.SpringDataCuentaUsuarioRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.Optional;

/**
 * ADAPTADOR del puerto UserRepository.
 * Construye el User a partir de persona + cuenta + la asignación de rol vigente.
 */
@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataCuentaUsuarioRepository cuentas;
    private final SpringDataAsignacionRolRepository asignaciones;

    public UserRepositoryAdapter(SpringDataCuentaUsuarioRepository cuentas,
                                 SpringDataAsignacionRolRepository asignaciones) {
        this.cuentas = cuentas;
        this.asignaciones = asignaciones;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return cuentas.findByCorreo(email).flatMap(cuenta -> {
            Optional<RoleAssignment> asignacionActiva = asignaciones.findByCuentaId(cuenta.getId()).stream()
                    .filter(a -> a.getVigenteHasta() == null)
                    .flatMap(a -> roleOf(a).map(role -> new RoleAssignment(role, a)).stream())
                    .min(Comparator.comparingInt(assignment -> assignment.role().ordinal()));

            return asignacionActiva.map(assignment -> CuentaUsuarioMapper.toDomain(
                    cuenta,
                    assignment.role(),
                    scopeOf(assignment),
                    assignment.asignacion().getNivelEducativoId(),
                    supervisorOf(assignment.asignacion())));
        });
    }

    private static Optional<Role> roleOf(AsignacionRolEntity asignacion) {
        try {
            return Optional.of(Role.valueOf(asignacion.getRol().getCodigo()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static RoleScope scopeOf(RoleAssignment assignment) {
        if (assignment.role() != Role.DIRECTOR) {
            return null;
        }
        return assignment.asignacion().getNivelEducativoId() == null
                ? RoleScope.INSTITUCION
                : RoleScope.NIVEL_EDUCATIVO;
    }

    private static Long supervisorOf(AsignacionRolEntity asignacion) {
        return asignacion.getReportaA() == null ? null : asignacion.getReportaA().getId();
    }

    private record RoleAssignment(Role role, AsignacionRolEntity asignacion) {
    }
}
