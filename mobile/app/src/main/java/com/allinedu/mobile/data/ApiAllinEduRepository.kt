package com.allinedu.mobile.data

import com.allinedu.mobile.data.dto.AttendanceEntryDto
import com.allinedu.mobile.data.dto.AttendanceRequestDto
import com.allinedu.mobile.data.dto.AttendanceResponseDto
import com.allinedu.mobile.data.dto.LoginRequestDto
import com.allinedu.mobile.data.dto.LoginResponseDto
import com.allinedu.mobile.data.dto.SectionDto
import com.allinedu.mobile.data.dto.StudentDto
import com.allinedu.mobile.domain.model.AttendanceEntry
import com.allinedu.mobile.domain.model.AttendanceStatus
import com.allinedu.mobile.domain.model.Section
import com.allinedu.mobile.domain.model.Student
import com.allinedu.mobile.domain.repository.AllinEduRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// ADAPTADOR del puerto AllinEduRepository.
// Es la ÚNICA clase de la app que conoce el backend (URLs, JSON, Ktor).
// En la web, esto es lo mismo que services/api.ts
class ApiAllinEduRepository(
    private val tokenStore: TokenStore,
    private val baseUrl: String,
) : AllinEduRepository {

    private val client = HttpClient {
        // Un 4xx/5xx lanza excepción (ClientRequestException en 4xx) en vez de
        // intentar leer el cuerpo como si fuera éxito: así un 401 no parece "sin conexión".
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override suspend fun login(email: String, password: String): String {
        val response = client.post("$baseUrl/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(email, password))
        }
        val dto: LoginResponseDto = response.body()
        tokenStore.saveToken(dto.token)
        return dto.name
    }

    override suspend fun getMySections(): List<Section> {
        val dtos: List<SectionDto> = client.get("$baseUrl/api/v1/sections/me") {
            bearerAuth(token())
        }.body()
        return dtos.map { Section(it.id, it.nombre, it.grado, it.descripcion) }
    }

    override suspend fun getSectionStudents(sectionId: Long): List<Student> {
        val dtos: List<StudentDto> = client.get("$baseUrl/api/v1/sections/$sectionId/students") {
            bearerAuth(token())
        }.body()
        return dtos.map { Student(it.id, it.name, it.code) }
    }

    override suspend fun saveAttendance(
        sectionId: Long,
        date: String,
        statusByStudent: Map<Long, AttendanceStatus>,
    ): List<AttendanceEntry> {
        val request = AttendanceRequestDto(
            date = date,
            attendance = statusByStudent.map { (studentId, status) ->
                AttendanceEntryDto(studentId, status.name)
            },
        )
        val dtos: List<AttendanceResponseDto> = client.post("$baseUrl/api/v1/sections/$sectionId/attendance") {
            contentType(ContentType.Application.Json)
            bearerAuth(token())
            setBody(request)
        }.body()
        return dtos.toDomain()
    }

    override suspend fun getAttendance(sectionId: Long, date: String): List<AttendanceEntry> {
        val dtos: List<AttendanceResponseDto> =
            client.get("$baseUrl/api/v1/sections/$sectionId/attendance?date=$date") {
                bearerAuth(token())
            }.body()
        return dtos.toDomain()
    }

    private fun token(): String =
        tokenStore.getToken() ?: throw IllegalStateException("No hay sesión iniciada")

    private fun List<AttendanceResponseDto>.toDomain(): List<AttendanceEntry> =
        map { AttendanceEntry(it.studentId, it.studentName, it.date, AttendanceStatus.valueOf(it.status)) }
}