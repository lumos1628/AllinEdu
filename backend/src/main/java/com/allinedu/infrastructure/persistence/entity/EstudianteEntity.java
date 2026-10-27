package com.allinedu.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "estudiantes")
public class EstudianteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id")
    private PersonaEntity persona;

    @Column(name = "codigo_estudiante", unique = true, nullable = false)
    private String codigoEstudiante;

    @Column(name = "identificador_trayectoria")
    private String identificadorTrayectoria;

    protected EstudianteEntity() {
    }

    public EstudianteEntity(Long id, PersonaEntity persona, String codigoEstudiante) {
        this.id = id;
        this.persona = persona;
        this.codigoEstudiante = codigoEstudiante;
    }

    public Long getId() {
        return id;
    }

    public PersonaEntity getPersona() {
        return persona;
    }

    public String getCodigoEstudiante() {
        return codigoEstudiante;
    }
}