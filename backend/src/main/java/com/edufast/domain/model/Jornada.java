package com.edufast.domain.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Modelo de dominio PURO: la pasada de lista de una sección en una fecha.
 */
public class Jornada {

    private final Long id;
    private final Long sectionId;
    private final LocalDate date;
    private final EstadoJornada estado;
    private final List<Attendance> registros;

    public Jornada(Long id, Long sectionId, LocalDate date, EstadoJornada estado, List<Attendance> registros) {
        this.id = id;
        this.sectionId = sectionId;
        this.date = date;
        this.estado = estado;
        this.registros = registros;
    }

    public Long getId() {
        return id;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public LocalDate getDate() {
        return date;
    }

    public EstadoJornada getEstado() {
        return estado;
    }

    public List<Attendance> getRegistros() {
        return registros;
    }
}