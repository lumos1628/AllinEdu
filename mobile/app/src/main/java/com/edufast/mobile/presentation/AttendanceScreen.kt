package com.edufast.mobile.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufast.mobile.domain.model.Section
import com.edufast.mobile.domain.model.Student
import com.edufast.mobile.domain.repository.EduFastRepository
import kotlinx.coroutines.launch

// Pantalla de asistencia: fecha + checkboxes + guardar. Igual que Attendance.tsx en la web.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    repository: EduFastRepository,
    section: Section,
    onBack: () -> Unit,
) {
    var students by remember { mutableStateOf<List<Student>>(emptyList()) }
    var present by remember { mutableStateOf<Map<Long, Boolean>>(emptyMap()) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(section.id) {
        try {
            students = repository.getSectionStudents(section.id)
            present = students.associate { it.id to true }
        } catch (e: Exception) {
            error = "No se pudieron cargar los alumnos. Revisa la conexión."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(section.descripcion) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("←") }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(students) { student ->
                    val isPresent = present[student.id] ?: true
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                present = present + (student.id to !isPresent)
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = isPresent,
                            onCheckedChange = { checked ->
                                present = present + (student.id to checked)
                            },
                        )
                        Column {
                            Text(student.name, style = MaterialTheme.typography.bodyLarge)
                            Text(student.code, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        message = null
                        error = null
                        try {
                            val saved = repository.saveAttendance(section.id, today(), present)
                            message = "Guardado: ${saved.count { it.present }} presentes"
                        } catch (e: Exception) {
                            error = "No se pudo guardar la asistencia"
                        }
                    }
                },
            ) {
                Text("Guardar asistencia")
            }
        }
    }
}

private fun today(): String = java.time.LocalDate.now().toString()