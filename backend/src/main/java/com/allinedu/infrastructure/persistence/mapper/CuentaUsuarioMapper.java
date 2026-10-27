package com.allinedu.infrastructure.persistence.mapper;

import com.allinedu.domain.model.Role;
import com.allinedu.domain.model.RoleScope;
import com.allinedu.domain.model.User;
import com.allinedu.infrastructure.persistence.entity.CuentaUsuarioEntity;

/**
 * Traductor entre CuentaUsuarioEntity y el modelo de dominio User.
 */
public final class CuentaUsuarioMapper {

    private CuentaUsuarioMapper() {
    }

    public static User toDomain(CuentaUsuarioEntity entity, Role role) {
        return toDomain(entity, role, null, null, null);
    }

    public static User toDomain(CuentaUsuarioEntity entity, Role role, RoleScope roleScope,
                                Short educationLevelId, Long supervisorUserId) {
        return new User(
                entity.getId(),
                entity.getPersona().getNombreCompleto(),
                entity.getCorreo(),
                entity.getPasswordHash(),
                role,
                roleScope,
                educationLevelId,
                supervisorUserId);
    }
}
