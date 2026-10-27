package com.allinedu.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "registros_asistencia",
        uniqueConstraints = @UniqueConstraint(columnNames = {"jornada_id", "ubicacion_matricula_id"}))
public class RegistroAsistenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jornada_id")
    private JornadaAsistenciaEntity jornada;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ubicacion_matricula_id")
    private UbicacionMatriculaEntity ubicacionMatricula;

    @Column(nullable = false)
    private String estado;

    @Column
    private String observacion;

    protected RegistroAsistenciaEntity() {
    }

    public RegistroAsistenciaEntity(Long id, JornadaAsistenciaEntity jornada,
                                    UbicacionMatriculaEntity ubicacionMatricula, String estado) {
        this.id = id;
        this.jornada = jornada;
        this.ubicacionMatricula = ubicacionMatricula;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public JornadaAsistenciaEntity getJornada() {
        return jornada;
    }

    public UbicacionMatriculaEntity getUbicacionMatricula() {
        return ubicacionMatricula;
    }

    public String getEstado() {
        return estado;
    }
}