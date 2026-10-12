package com.edufast.domain.model;

/**
 * Modelo de dominio PURO: sin anotaciones de frameworks.
 * La versión con JPA vive en infrastructure/persistence/entity/UserEntity.
 */
public class User {

    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final Role role;
    private final RoleScope roleScope;
    private final Short educationLevelId;
    private final Long supervisorUserId;

    public User(Long id, String name, String email, String password, Role role) {
        this(id, name, email, password, role, null, null, null);
    }

    public User(Long id, String name, String email, String password, Role role,
                RoleScope roleScope, Short educationLevelId, Long supervisorUserId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.roleScope = roleScope;
        this.educationLevelId = educationLevelId;
        this.supervisorUserId = supervisorUserId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public RoleScope getRoleScope() {
        return roleScope;
    }

    public Short getEducationLevelId() {
        return educationLevelId;
    }

    public Long getSupervisorUserId() {
        return supervisorUserId;
    }
}
