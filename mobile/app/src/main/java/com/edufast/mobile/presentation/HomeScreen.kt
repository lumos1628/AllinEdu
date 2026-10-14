package com.edufast.mobile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Calendar

// Colores por eslabón de la cadena
object EduColors {
    val PrimaryDark = Color(0xFF1B5E7C)
    val Accent = Color(0xFF2E8B97)
    val Success = Color(0xFF4A7C59)
    val Warning = Color(0xFFB7791F)
    val Highlight = Color(0xFF6B4C9A)
    val Secondary = Color(0xFFB85A5A)
    val Muted = Color(0xFF5A6C7D)
    val Background = Color(0xFFFAFBFC)
}

// Saludo según la hora del día
fun getGreeting(hour: Int): String = when (hour) {
    in 5..11 -> "Buenos días"
    in 12..17 -> "Buenas tardes"
    else -> "Buenas noches"
}

fun getCurrentHour(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

// Un eslabón de la cadena (ver ALCANCE.md §2)
data class ModuleItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val isEnabled: Boolean = false,
)

// Orden de la cadena: se respeta tal cual para que el docente vea el ciclo completo.
// Solo "Aplicar en clase" está construido; el resto se anuncia sin ser navegable.
private val chainModules = listOf(
    ModuleItem("planning", "Planificar", "Sesión, ficha diferenciada e instrumento", Icons.Default.Edit, EduColors.PrimaryDark),
    ModuleItem("attendance", "Aplicar en clase", "Asistencia de tus secciones", Icons.Default.CheckCircle, EduColors.Success, isEnabled = true),
    ModuleItem("evaluation", "Evaluar", "Criterios y evidencia por competencia", Icons.Default.Star, EduColors.Warning),
    ModuleItem("reinforcement", "Reforzar", "Diagnóstico, práctica y repaso", Icons.Default.Refresh, EduColors.Highlight),
    ModuleItem("communication", "Comunicar", "Avisos a familias con horario visible", Icons.Default.Email, EduColors.Secondary),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String = "Profesor",
    onModuleSelect: (String) -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AllinEdu", fontWeight = FontWeight.Bold, color = EduColors.PrimaryDark)
                        Text(
                            "${getGreeting(getCurrentHour())}, $userName",
                            style = MaterialTheme.typography.bodySmall,
                            color = EduColors.Muted,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(EduColors.Background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Tu ciclo de clase",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EduColors.PrimaryDark,
                )
                Text(
                    "Planificar → Aplicar → Evaluar → Reforzar",
                    style = MaterialTheme.typography.bodySmall,
                    color = EduColors.Muted,
                )
            }
            items(chainModules.withIndex().toList(), key = { it.value.id }) { (index, module) ->
                ModuleCard(
                    module = module,
                    step = index + 1,
                    onClick = { onModuleSelect(module.id) },
                )
            }
        }
    }
}

@Composable
private fun ModuleCard(module: ModuleItem, step: Int, onClick: () -> Unit) {
    val status = if (module.isEnabled) "Disponible" else "Próximamente"
    val description = "Paso $step: ${module.title}. ${module.description}. $status"

    Card(
        onClick = onClick,
        enabled = module.isEnabled,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            disabledContainerColor = Color(0xFFF3F4F6),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (module.isEnabled) 3.dp else 0.dp,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = description
                if (!module.isEnabled) disabled()
            },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val tint = if (module.isEnabled) module.color else EduColors.Muted
            Box(
                modifier = Modifier
                    .size(if (module.isEnabled) 52.dp else 40.dp)
                    .background(tint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(module.icon, contentDescription = null, tint = tint)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Paso $step · $status",
                    style = MaterialTheme.typography.labelSmall,
                    color = tint,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    module.title,
                    style = if (module.isEnabled) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (module.isEnabled) EduColors.PrimaryDark else EduColors.Muted,
                )
                Text(
                    module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = EduColors.Muted,
                )
            }
            if (module.isEnabled) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = module.color)
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(userName = "María García")
    }
}
