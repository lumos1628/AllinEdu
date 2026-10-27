package com.allinedu.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "jornadas_asistencia",
        uniqueConstraints = @UniqueConstraint(columnNames = {"seccion_id", "fecha"}))
public class JornadaAsistenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seccion_id")
    private SeccionEntity seccion;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrada_por")
    private CuentaUsuarioEntity registradaPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmada_por")
    private CuentaUsuarioEntity confirmadaPor;

    @Column(name = "confirmada_at")
    private Instant confirmadaAt;

    protected JornadaAsistenciaEntity() {
    }

    public JornadaAsistenciaEntity(Long id, SeccionEntity seccion, LocalDate fecha, String estado,
                                   CuentaUsuarioEntity registradaPor) {
        this.id = id;
        this.seccion = seccion;
        this.fecha = fecha;
        this.estado = estado;
        this.registradaPor = registradaPor;
    }

    public Long getId() {
        return id;
    }

    public SeccionEntity getSeccion() {
        return seccion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setConfirmadaPor(CuentaUsuarioEntity confirmadaPor) {
        this.confirmadaPor = confirmadaPor;
    }

    public void setConfirmadaAt(Instant confirmadaAt) {
        this.confirmadaAt = confirmadaAt;
    }
}