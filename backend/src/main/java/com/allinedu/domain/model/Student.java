package com.allinedu.domain.model;

/**
 * Modelo de dominio PURO: sin anotaciones de frameworks.
 */
public class Student {

    private final Long id;
    private final String name;
    private final String code;

    public Student(Long id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
