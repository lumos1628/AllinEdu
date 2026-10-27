package com.allinedu.application.dto;

import com.allinedu.domain.model.Student;

public record StudentResponse(
        Long id,
        String name,
        String code) {

    public static StudentResponse from(Student student) {
        return new StudentResponse(student.getId(), student.getName(), student.getCode());
    }
}