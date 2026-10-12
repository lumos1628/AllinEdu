package com.edufast.mobile.data.dto

import kotlinx.serialization.Serializable

// DTOs: la forma exacta del JSON que devuelve el backend.
// Cada @Serializable mapea un JSON a un objeto Kotlin.

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class LoginResponseDto(
    val token: String,
    val userId: Long,
    val name: String,
    val email: String,
    val role: String,
    val roleScope: String? = null,
    val educationLevelId: Short? = null,
    val supervisorUserId: Long? = null,
)

@Serializable
data class SectionDto(
    val id: Long,
    val nombre: String,
    val grado: String,
    val descripcion: String,
)

@Serializable
data class StudentDto(
    val id: Long,
    val name: String,
    val code: String,
)

@Serializable
data class AttendanceRequestDto(
    val date: String,
    val attendance: List<AttendanceEntryDto>,
)

@Serializable
data class AttendanceEntryDto(
    val studentId: Long,
    val present: Boolean,
)

@Serializable
data class AttendanceResponseDto(
    val studentId: Long,
    val studentName: String,
    val date: String,
    val present: Boolean,
)
