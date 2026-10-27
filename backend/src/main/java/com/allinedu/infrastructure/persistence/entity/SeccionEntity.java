package com.allinedu.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "secciones",
        uniqueConstraints = @UniqueConstraint(columnNames = {"institucion_id", "anio_escolar_id", "grado_id", "nombre"}))
public class SeccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anio_escolar_id")
    private AnioEscolarEntity anioEscolar;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institucion_id")
    private InstitucionEntity institucion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grado_id")
    private GradoEntity grado;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "capacidad_maxima")
    private Short capacidadMaxima;

    @Column(nullable = false)
    private boolean activa;

    protected SeccionEntity() {
    }

    public SeccionEntity(Long id, AnioEscolarEntity anioEscolar, InstitucionEntity institucion,
                         GradoEntity grado, String nombre) {
        this.id = id;
        this.anioEscolar = anioEscolar;
        this.institucion = institucion;
        this.grado = grado;
        this.nombre = nombre;
        this.activa = true;
    }

    public Long getId() {
        return id;
    }

    public AnioEscolarEntity getAnioEscolar() {
        return anioEscolar;
    }

    public InstitucionEntity getInstitucion() {
        return institucion;
    }

    public GradoEntity getGrado() {
        return grado;
    }

    public String getNombre() {
        return nombre;
    }
}