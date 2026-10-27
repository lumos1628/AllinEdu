package com.allinedu.mobile.presentation

import androidx.compose.foundation.BorderStroke
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
import com.allinedu.mobile.presentation.theme.AllinColors
import com.allinedu.mobile.presentation.theme.ThemeToggleButton
import com.allinedu.mobile.presentation.theme.Wordmark
import java.util.Calendar

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
    val isEnabled: Boolean = false,
)

// Orden de la cadena: se respeta tal cual para que el docente vea el ciclo completo.
// Solo "Aplicar en clase" está construido; el resto se anuncia sin ser navegable.
private val chainModules = listOf(
    ModuleItem("planning", "Planificar", "Sesión, ficha diferenciada e instrumento", Icons.Default.Edit),
    ModuleItem("attendance", "Aplicar en clase", "Asistencia de tus secciones", Icons.Default.CheckCircle, isEnabled = true),
    ModuleItem("evaluation", "Evaluar", "Criterios y evidencia por competencia", Icons.Default.Star),
    ModuleItem("reinforcement", "Reforzar", "Diagnóstico, práctica y repaso", Icons.Default.Refresh),
    ModuleItem("communication", "Comunicar", "Avisos a familias con horario visible", Icons.Default.Email),
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
                        Wordmark(fontSize = 20)
                        Text(
                            "${getGreeting(getCurrentHour())}, $userName",
                            style = MaterialTheme.typography.bodySmall,
                            color = AllinColors.Muted,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AllinColors.Card),
                actions = { ThemeToggleButton() },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AllinColors.Background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Tu ciclo de clase",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AllinColors.Text,
                )
                Text(
                    "Planificar → Aplicar → Evaluar → Reforzar",
                    style = MaterialTheme.typography.bodySmall,
                    color = AllinColors.Muted,
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
            containerColor = AllinColors.Card,
            disabledContainerColor = AllinColors.Background,
        ),
        // El eslabón disponible lleva el filo dorado de la marca
        border = if (module.isEnabled) BorderStroke(1.5.dp, AllinColors.Primary) else BorderStroke(1.dp, AllinColors.Border),
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
            val tint = if (module.isEnabled) AllinColors.GoldText else AllinColors.Muted
            Box(
                modifier = Modifier
                    .size(if (module.isEnabled) 52.dp else 40.dp)
                    .background(if (module.isEnabled) AllinColors.Primary.copy(alpha = 0.15f) else AllinColors.Border.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(module.icon, contentDescription = null, tint = tint)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Paso $step · $status",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (module.isEnabled) AllinColors.GoldText else AllinColors.Muted,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    module.title,
                    style = if (module.isEnabled) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (module.isEnabled) AllinColors.Text else AllinColors.Muted,
                )
                Text(
                    module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AllinColors.Muted,
                )
            }
            if (module.isEnabled) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AllinColors.GoldText)
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
