package com.edufast.mobile.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

// Colores institucionales educativos
object EduColors {
    val PrimaryDark = Color(0xFF1B5E7C)      // Azul educativo oscuro
    val Accent = Color(0xFF2E8B97)            // Azul-verde (aplicar)
    val Success = Color(0xFF4A7C59)           // Verde educativo (asistencia)
    val Warning = Color(0xFFD4A574)           // Ámbar educativo (evaluación)
    val Highlight = Color(0xFF6B4C9A)         // Púrpura educativo (refuerzo)
    val Secondary = Color(0xFFB85A5A)         // Rojo académico (comunicación)
    val Disabled = Color(0xFFA6A6A6)          // Gris neutro
}

// Helper para greeting dinámico según la hora
fun getGreeting(hour: Int): String = when {
    hour in 5..11 -> "Buenos días"
    hour in 12..17 -> "Buenas tardes"
    else -> "Buenas noches"
}

// Obtener la hora actual
fun getCurrentHour(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

// Shape personalizado: rectángulo con esquina superior derecha cortada y base levemente angulada
class EducativeCardShape(
    private val cornerRadius: Float = 20f,
    private val topCut: Float = 16f,
    private val bottomSkew: Float = 4f
) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): androidx.compose.ui.graphics.Outline {
        val path = Path().apply {
            // Esquina superior izquierda redondeada
            moveTo(0f, cornerRadius)
            quadraticTo(cornerRadius, 0f, 0f, 0f)

            // Línea superior hasta el corte
            lineTo(size.width - topCut, 0f)

            // Corte diagonal en esquina superior derecha
            lineTo(size.width, topCut)

            // Lado derecho
            lineTo(size.width, size.height - cornerRadius)

            // Esquina inferior derecha redondeada
            quadraticTo(size.width - cornerRadius, size.height, size.width, size.height)

            // Base con ligero sesgo
            lineTo(bottomSkew, size.height)

            // Esquina inferior izquierda redondeada
            quadraticTo(0f, size.height - cornerRadius, 0f, size.height)

            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

// Modelo de un módulo de la cadena
data class ModuleItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val position: Int,
    val total: Int,
    val isEnabled: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String = "Profesor",
    onModuleSelect: (String) -> Unit = {}
) {
    val modules = listOf(
        ModuleItem(
            id = "planning",
            title = "Planificación",
            description = "Prepara sesiones, fichas e instrumentos",
            icon = Icons.Default.Edit,
            color = EduColors.PrimaryDark,
            position = 1,
            total = 5,
            isEnabled = false
        ),
        ModuleItem(
            id = "attendance",
            title = "Aplicar",
            description = "Asistencia y evidencia en clase",
            icon = Icons.Default.CheckCircle,
            color = EduColors.Success,
            position = 2,
            total = 5,
            isEnabled = true
        ),
        ModuleItem(
            id = "evaluation",
            title = "Evaluación",
            description = "Valorar criterios y evidencias",
            icon = Icons.Default.Info,
            color = EduColors.Warning,
            position = 3,
            total = 5,
            isEnabled = false
        ),
        ModuleItem(
            id = "reinforcement",
            title = "Refuerzo",
            description = "Diagnóstico, práctica y repaso",
            icon = Icons.Default.Build,
            color = EduColors.Highlight,
            position = 4,
            total = 5,
            isEnabled = false
        ),
        ModuleItem(
            id = "communication",
            title = "Comunicación",
            description = "Reportes y avisos a familias",
            icon = Icons.Default.Notifications,
            color = EduColors.Secondary,
            position = 5,
            total = 5,
            isEnabled = false
        )
    )

    // Reordenar: módulos habilitados primero
    val sortedModules = modules.sortedBy { !it.isEnabled }
    val attendanceModule = modules.find { it.id == "attendance" }

    Scaffold(
        topBar = {
            HomeTopAppBar(userName = userName)
        },
        floatingActionButton = {
            // FAB para acceso rápido a "Aplicar" (módulo habilitado)
            if (attendanceModule?.isEnabled == true) {
                ExtendedFloatingActionButton(
                    text = {
                        Text(
                            "Tomar asistencia",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    onClick = { onModuleSelect("attendance") },
                    modifier = Modifier.semantics {
                        contentDescription = "Acceso rápido a tomar asistencia"
                    },
                    containerColor = EduColors.Success,
                    contentColor = Color.White
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFAFBFC))
        ) {
            // Subtítulo con la cadena pedagógica
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    "La cadena pedagógica",
                    style = MaterialTheme.typography.titleSmall,
                    color = EduColors.PrimaryDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Planificar → Aplicar → Evaluar → Reforzar → Comunicar",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF7F8C8D),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Grid de módulos - una columna para mejor legibilidad
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(sortedModules.size) { index ->
                    val module = sortedModules[index]
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(300)) +
                                slideInVertically(
                                    initialOffsetY = { 100 },
                                    animationSpec = tween(300 + index * 50)
                                ),
                        label = "moduleCardEnter"
                    ) {
                        ModuleCard(
                            module = module,
                            onClick = { if (module.isEnabled) onModuleSelect(module.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar(userName: String) {
    val greeting = getGreeting(getCurrentHour())

    TopAppBar(
        title = {
            Column(
                modifier = Modifier.semantics {
                    contentDescription = "Bienvenida del usuario"
                }
            ) {
                Text(
                    "$greeting, $userName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EduColors.PrimaryDark,
                    fontSize = 16.sp
                )
                Text(
                    "AllinEdu — Cadena pedagógica",
                    style = MaterialTheme.typography.labelSmall,
                    color = EduColors.Accent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        ),
        modifier = Modifier.shadow(elevation = 2.dp),
        actions = {
            IconButton(
                onClick = {},
                modifier = Modifier.semantics {
                    contentDescription = "Abrir configuración"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = EduColors.PrimaryDark.copy(alpha = 0.6f)
                )
            }
        }
    )
}

@Composable
private fun ModuleCard(
    module: ModuleItem,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.96f
            isHovered && module.isEnabled -> 1.02f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    val elevation by animateDpAsState(
        targetValue = when {
            isPressed -> 16.dp
            isHovered && module.isEnabled -> 12.dp
            else -> 4.dp
        },
        animationSpec = tween(durationMillis = 200),
        label = "cardElevation"
    )

    val bgColor = if (module.isEnabled) Color.White else Color(0xFFF5F5F5)
    val progressAlpha = (module.position - 1) / (module.total - 1).toFloat()
    val interactionSource = remember { MutableInteractionSource() }

    val cardDescription = buildString {
        append("${module.title}. ${module.description}. ")
        append("Paso ${module.position} de ${module.total}. ")
        append(if (module.isEnabled) "Disponible" else "Próximamente disponible")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(EducativeCardShape())
            .shadow(elevation = elevation, shape = EducativeCardShape(), clip = true)
            .scale(scale)
            .semantics {
                contentDescription = cardDescription
                if (!module.isEnabled) disabled()
            }
            .clickable(
                enabled = module.isEnabled,
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = module.color.copy(alpha = 0.2f))
            ) {
                isPressed = true
                onClick()
                isPressed = false
            },
        shape = EducativeCardShape(),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val accentPath = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(4f, 0f)
                        lineTo(4f, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    val gradientBrush = Brush.linearGradient(
                        colors = listOf(module.color, module.color.copy(alpha = 0.5f)),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height)
                    )
                    onDrawBehind {
                        drawPath(accentPath, gradientBrush)
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                // Encabezado con icono y número
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = module.color.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = module.icon,
                            contentDescription = module.title,
                            tint = module.color,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Número del paso
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "Paso",
                            style = MaterialTheme.typography.labelSmall,
                            color = module.color.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 9.sp
                        )
                        Text(
                            text = "${module.position}",
                            style = MaterialTheme.typography.displaySmall,
                            color = module.color,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                    }
                }

                // Contenido: Título y descripción
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = module.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = EduColors.PrimaryDark,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = module.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF5A6C7D),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        maxLines = 2
                    )
                }

                // Barra de progresión en la cadena (más grande)
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Progreso en la cadena",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF95A5A6),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color(0xFFECF0F1), shape = RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = progressAlpha)
                                .background(module.color, shape = RoundedCornerShape(3.dp))
                        )
                    }
                }

                // Badge de estado (mejorado con mejor contraste)
                if (!module.isEnabled) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.End)
                            .clip(RoundedCornerShape(8.dp))
                            .semantics {
                                contentDescription = "Próximamente disponible"
                            },
                        color = EduColors.Disabled.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⏱ Próximamente",
                            style = MaterialTheme.typography.labelSmall,
                            color = EduColors.Disabled.copy(alpha = 0.9f),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// Preview en Android Studio
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFFAFBFC)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(userName = "María García")
    }
}
