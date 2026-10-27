package com.allinedu.mobile.domain.repository

import com.allinedu.mobile.domain.model.AttendanceEntry
import com.allinedu.mobile.domain.model.AttendanceStatus
import com.allinedu.mobile.domain.model.Section
import com.allinedu.mobile.domain.model.Student

// PUERTO del dominio: el contrato que la app necesita.
// La implementación (ApiAllinEduRepository en data/) habla con el backend.
// Si mañana cambia el backend, solo cambia la implementación, no este contrato.
interface AllinEduRepository {

    // Guarda la sesión y devuelve el nombre del docente para saludarlo.
    suspend fun login(email: String, password: String): String

    suspend fun getMySections(): List<Section>

    suspend fun getSectionStudents(sectionId: Long): List<Student>

    suspend fun saveAttendance(sectionId: Long, date: String, statusByStudent: Map<Long, AttendanceStatus>): List<AttendanceEntry>

    suspend fun getAttendance(sectionId: Long, date: String): List<AttendanceEntry>
}