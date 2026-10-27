package com.allinedu.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cuentas_usuario")
public class CuentaUsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id")
    private PersonaEntity persona;

    @Column(unique = true, nullable = false)
    private String correo;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String estado;

    protected CuentaUsuarioEntity() {
    }

    public CuentaUsuarioEntity(Long id, PersonaEntity persona, String correo, String passwordHash, String estado) {
        this.id = id;
        this.persona = persona;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public PersonaEntity getPersona() {
        return persona;
    }

    public String getCorreo() {
        return correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEstado() {
        return estado;
    }
}