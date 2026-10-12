package com.edufast.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "asignaciones_docentes")
public class AsignacionDocenteEntity {

    private static final String FUNCION_DOCENTE = "DOCENTE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_usuario_id")
    private CuentaUsuarioEntity cuenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seccion_id")
    private SeccionEntity seccion;

    @Column(name = "es_tutor", nullable = false)
    private boolean esTutor;

    @Column(name = "funcion", nullable = false)
    private String funcion = FUNCION_DOCENTE;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;

    @Column(name = "vigente_hasta")
    private LocalDate vigenteHasta;

    protected AsignacionDocenteEntity() {
    }

    public AsignacionDocenteEntity(Long id, CuentaUsuarioEntity cuenta, SeccionEntity seccion) {
        this.id = id;
        this.cuenta = cuenta;
        this.seccion = seccion;
        this.vigenteDesde = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public CuentaUsuarioEntity getCuenta() {
        return cuenta;
    }

    public SeccionEntity getSeccion() {
        return seccion;
    }

    public String getFuncion() {
        return funcion;
    }

    public LocalDate getVigenteDesde() {
        return vigenteDesde;
    }

    public LocalDate getVigenteHasta() {
        return vigenteHasta;
    }
}
