package com.allinedu.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "instituciones_educativas")
public class InstitucionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_modular", unique = true)
    private String codigoModular;

    @Column(name = "codigo_local")
    private String codigoLocal;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "tipo_gestion", nullable = false)
    private String tipoGestion;

    protected InstitucionEntity() {
    }

    public InstitucionEntity(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}