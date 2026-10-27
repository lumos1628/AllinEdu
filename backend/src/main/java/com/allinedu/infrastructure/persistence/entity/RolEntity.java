package com.allinedu.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class RolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(unique = true, nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    protected RolEntity() {
    }

    public RolEntity(Long id, String codigo, String nombre) {
        this.id = id == null ? null : id.shortValue();
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public Short getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }
}