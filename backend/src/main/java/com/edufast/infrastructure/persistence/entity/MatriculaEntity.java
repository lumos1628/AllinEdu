package com.edufast.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "matriculas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"estudiante_id", "anio_escolar_id"}))
public class MatriculaEntity {

    private static final String ESTADO_ACTIVA = "ACTIVA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id")
    private EstudianteEntity estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anio_escolar_id")
    private AnioEscolarEntity anioEscolar;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institucion_id")
    private InstitucionEntity institucion;

    @Column(name = "fecha_matricula", nullable = false)
    private LocalDate fechaMatricula;

    @Column(nullable = false)
    private String estado;

    protected MatriculaEntity() {
    }

    public MatriculaEntity(Long id, EstudianteEntity estudiante, AnioEscolarEntity anioEscolar) {
        this.id = id;
        this.estudiante = estudiante;
        this.anioEscolar = anioEscolar;
        this.fechaMatricula = LocalDate.now();
        this.estado = ESTADO_ACTIVA;
    }

    public Long getId() {
        return id;
    }

    public EstudianteEntity getEstudiante() {
        return estudiante;
    }

    public AnioEscolarEntity getAnioEscolar() {
        return anioEscolar;
    }

    public String getEstado() {
        return estado;
    }
}