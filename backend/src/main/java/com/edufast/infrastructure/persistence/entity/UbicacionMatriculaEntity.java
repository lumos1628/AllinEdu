package com.edufast.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "ubicaciones_matricula")
public class UbicacionMatriculaEntity {

    private static final String MOTIVO_MATRICULA_INICIAL = "MATRICULA_INICIAL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matricula_id")
    private MatriculaEntity matricula;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seccion_id")
    private SeccionEntity seccion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(nullable = false)
    private String motivo;

    protected UbicacionMatriculaEntity() {
    }

    public UbicacionMatriculaEntity(Long id, MatriculaEntity matricula, SeccionEntity seccion, LocalDate fechaInicio) {
        this.id = id;
        this.matricula = matricula;
        this.seccion = seccion;
        this.fechaInicio = fechaInicio;
        this.motivo = MOTIVO_MATRICULA_INICIAL;
    }

    public Long getId() {
        return id;
    }

    public MatriculaEntity getMatricula() {
        return matricula;
    }

    public SeccionEntity getSeccion() {
        return seccion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }
}