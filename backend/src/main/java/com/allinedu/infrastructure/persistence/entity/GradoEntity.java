package com.allinedu.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "grados_educativos")
public class GradoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false)
    private Short numero;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String codigo;

    protected GradoEntity() {
    }

    public GradoEntity(Long id, Short numero, String nombre, String codigo) {
        this.id = id == null ? null : id.shortValue();
        this.numero = numero;
        this.nombre = nombre;
        this.codigo = codigo;
    }

    public Long getId() {
        return id == null ? null : id.longValue();
    }

    public Short getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigo() {
        return codigo;
    }
}