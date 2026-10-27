package com.allinedu.domain.model;

/**
 * Modelo de dominio PURO: una sección de un grado en un año escolar.
 * Ejemplo: "4.º de primaria", sección "A".
 */
public class Section {

    private final Long id;
    private final String nombre;
    private final String gradoNombre;
    private final String gradoCodigo;

    public Section(Long id, String nombre, String gradoNombre, String gradoCodigo) {
        this.id = id;
        this.nombre = nombre;
        this.gradoNombre = gradoNombre;
        this.gradoCodigo = gradoCodigo;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getGradoNombre() {
        return gradoNombre;
    }

    public String getGradoCodigo() {
        return gradoCodigo;
    }

    public String getDescripcion() {
        return gradoNombre + " - " + nombre;
    }
}