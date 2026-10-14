package com.edufast.mobile.domain.model

// Estado de asistencia. TARDANZA cuenta como presente en los resúmenes.
enum class AttendanceStatus { PRESENTE, AUSENTE, TARDANZA }

// Entidad de dominio: un registro de asistencia de un alumno en una fecha.
data class AttendanceEntry(
    val studentId: Long,
    val studentName: String,
    val date: String,
    val status: AttendanceStatus,
)
