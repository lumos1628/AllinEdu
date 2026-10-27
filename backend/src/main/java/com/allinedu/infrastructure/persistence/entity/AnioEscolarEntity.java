package com.allinedu.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "anios_escolares")
public class AnioEscolarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institucion_id")
    private InstitucionEntity institucion;

    @Column(nullable = false)
    private Short anio;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    private String estado;

    protected AnioEscolarEntity() {
    }

    public AnioEscolarEntity(Long id, InstitucionEntity institucion, Short anio) {
        this.id = id;
        this.institucion = institucion;
        this.anio = anio;
        this.estado = "ACTIVO";
    }

    public Long getId() {
        return id;
    }

    public Short getAnio() {
        return anio;
    }
}