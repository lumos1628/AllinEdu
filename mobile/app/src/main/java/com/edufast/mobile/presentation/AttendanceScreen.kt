package com.edufast.mobile.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edufast.mobile.domain.model.AttendanceStatus
import com.edufast.mobile.domain.model.Section
import com.edufast.mobile.domain.model.Student
import com.edufast.mobile.domain.repository.EduFastRepository
import com.edufast.mobile.presentation.theme.AllinColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot

// Colores y texto de cada estado. El texto repite lo que dice el color,
// para no depender solo de él.
private data class StatusStyle(val label: String, val background: Color, val border: Color, val text: Color)

private fun styleOf(status: AttendanceStatus): StatusStyle = when (status) {
    AttendanceStatus.PRESENTE -> StatusStyle("Asistió", AllinColors.PresentBg, AllinColors.Present, AllinColors.PresentText)
    AttendanceStatus.TARDANZA -> StatusStyle("Tardanza", AllinColors.LateBg, AllinColors.Late, AllinColors.LateText)
    AttendanceStatus.AUSENTE -> StatusStyle("Faltó", AllinColors.AbsentBg, AllinColors.Absent, AllinColors.AbsentText)
}

// Cada toque avanza al siguiente estado: asistió → faltó → tardanza (igual que la web).
private fun AttendanceStatus.next(): AttendanceStatus = when (this) {
    AttendanceStatus.PRESENTE -> AttendanceStatus.AUSENTE
    AttendanceStatus.AUSENTE -> AttendanceStatus.TARDANZA
    AttendanceStatus.TARDANZA -> AttendanceStatus.PRESENTE
}

private val CardShape = RoundedCornerShape(16.dp)
private val RailColor = AllinColors.Rail

// Pantalla de asistencia: lista vertical de tarjetas + guardar. Igual que Attendance.tsx en la web.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    repository: EduFastRepository,
    section: Section,
    onBack: () -> Unit,
) {
    var students by remember { mutableStateOf<List<Student>>(emptyList()) }
    var statuses by remember { mutableStateOf<Map<Long, AttendanceStatus>>(emptyMap()) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val reduceMotion = rememberReduceMotion()
    // Alumnos que ya hicieron su entrada: al volver a verlos con scroll no se repite.
    val revealed = remember(section.id) { mutableSetOf<Long>() }

    LaunchedEffect(section.id) {
        try {
            students = repository.getSectionStudents(section.id)
        } catch (e: Exception) {
            error = "No se pudieron cargar los alumnos. Revisa la conexión."
            return@LaunchedEffect
        }
        val allPresent = students.associate { it.id to AttendanceStatus.PRESENTE }
        // Si ya se tomó asistencia hoy (desde la web u otro dispositivo), se muestra esa;
        // si no, todos parten como presentes. Igual que Attendance.tsx en la web.
        statuses = try {
            allPresent + repository.getAttendance(section.id, today())
                .associate { it.studentId to it.status }
        } catch (e: Exception) {
            allPresent
        }
    }

    val counts = statuses.values.groupingBy { it }.eachCount()

    Scaffold(
        containerColor = AllinColors.Background,
        topBar = {
            TopAppBar(
                title = { Text(section.descripcion, color = AllinColors.Text, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AllinColors.Card),
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("←") }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Text(
                "Toca una tarjeta para cambiar: asistió → faltó → tardanza",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                "Asistieron ${counts[AttendanceStatus.PRESENTE] ?: 0} · " +
                    "Tardanzas ${counts[AttendanceStatus.TARDANZA] ?: 0} · " +
                    "Faltaron ${counts[AttendanceStatus.AUSENTE] ?: 0}",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            Box(Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp),
                ) {
                    itemsIndexed(students, key = { _, s -> s.id }) { index, student ->
                        val status = statuses[student.id] ?: AttendanceStatus.PRESENTE
                        val animateEntrance = remember(student.id) {
                            !reduceMotion && revealed.add(student.id)
                        }
                        StudentRow(
                            student = student,
                            status = status,
                            isFirst = index == 0,
                            isLast = index == students.lastIndex,
                            entranceDelayMs = if (animateEntrance) index.coerceAtMost(10) * 45L else null,
                            reduceMotion = reduceMotion,
                            onClick = { statuses = statuses + (student.id to status.next()) },
                        )
                    }
                }

                ScrollButtons(
                    canGoUp = listState.canScrollBackward,
                    canGoDown = listState.canScrollForward,
                    onUp = { scope.launch { listState.animateScrollToItem(0) } },
                    onDown = {
                        scope.launch { listState.animateScrollToItem((students.lastIndex).coerceAtLeast(0)) }
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 12.dp),
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        message = null
                        error = null
                        try {
                            repository.saveAttendance(section.id, today(), statuses)
                            message = "Asistencia guardada"
                        } catch (e: Exception) {
                            error = "No se pudo guardar la asistencia"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).heightIn(min = 48.dp),
            ) {
                Text("Guardar asistencia")
            }
        }
    }
}

// Botones flotantes para subir y bajar; cada uno aparece solo si hay hacia dónde ir.
@Composable
private fun ScrollButtons(
    canGoUp: Boolean,
    canGoDown: Boolean,
    onUp: () -> Unit,
    onDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AnimatedVisibility(visible = canGoUp, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
            SmallFloatingActionButton(onClick = onUp, containerColor = AllinColors.Primary, contentColor = AllinColors.OnPrimary) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Ir al primer alumno")
            }
        }
        AnimatedVisibility(visible = canGoDown, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
            SmallFloatingActionButton(onClick = onDown, containerColor = AllinColors.Primary, contentColor = AllinColors.OnPrimary) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Ir al último alumno")
            }
        }
    }
}

// Una fila: el riel del pase de lista a la izquierda y la tarjeta del alumno.
@Composable
private fun StudentRow(
    student: Student,
    status: AttendanceStatus,
    isFirst: Boolean,
    isLast: Boolean,
    entranceDelayMs: Long?,
    reduceMotion: Boolean,
    onClick: () -> Unit,
) {
    // Entrada escalonada: aparece ya crecida y se asienta (snap + animate, ver skill).
    val enter = remember(student.id) { Animatable(if (entranceDelayMs != null) 0f else 1f) }
    LaunchedEffect(student.id) {
        if (entranceDelayMs != null) {
            delay(entranceDelayMs)
            enter.animateTo(1f, tween(280, easing = FastOutSlowInEasing))
        }
    }

    val dotColor by animateColorAsState(
        styleOf(status).border,
        animationSpec = if (reduceMotion) tween(0) else tween(250),
        label = "railDot",
    )

    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .graphicsLayer {
                alpha = enter.value
                translationY = (1f - enter.value) * 24.dp.toPx()
            },
    ) {
        RollCallRail(
            isFirst = isFirst,
            isLast = isLast,
            dotColor = dotColor,
            modifier = Modifier.width(28.dp).fillMaxHeight(),
        )
        StudentCard(
            student = student,
            status = status,
            reduceMotion = reduceMotion,
            onClick = onClick,
            modifier = Modifier.weight(1f).padding(vertical = 6.dp),
        )
    }
}

// Riel del pase de lista: cada fila dibuja su tramo de línea y su punto, así la línea
// se mueve con el scroll sin desfase (se dibuja dentro del item, no encima de la lista).
@Composable
private fun RollCallRail(isFirst: Boolean, isLast: Boolean, dotColor: Color, modifier: Modifier) {
    Spacer(
        modifier.drawWithCache {
            val stroke = 2.dp.toPx()
            val dotRadius = 6.dp.toPx()
            val ringRadius = 9.dp.toPx()
            val x = size.width / 2f
            val cy = size.height / 2f
            onDrawBehind {
                if (!isFirst) {
                    drawLine(RailColor, Offset(x, 0f), Offset(x, cy - ringRadius), stroke, StrokeCap.Round)
                }
                if (!isLast) {
                    drawLine(RailColor, Offset(x, cy + ringRadius), Offset(x, size.height), stroke, StrokeCap.Round)
                }
                drawCircle(dotColor.copy(alpha = 0.25f), ringRadius, Offset(x, cy))
                drawCircle(dotColor, dotRadius, Offset(x, cy))
            }
        },
    )
}

@Composable
private fun StudentCard(
    student: Student,
    status: AttendanceStatus,
    reduceMotion: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = styleOf(status)
    val interactions = remember { MutableInteractionSource() }

    // Punto donde el docente tocó: de ahí sale la "tinta" del nuevo color.
    var origin by remember { mutableStateOf(Offset.Unspecified) }
    LaunchedEffect(interactions) {
        interactions.interactions.collect {
            if (it is PressInteraction.Press) origin = it.pressPosition
        }
    }

    // Color de fondo bajo la tinta y color de la tinta que se expande.
    var baseColor by remember { mutableStateOf(style.background) }
    var inkColor by remember { mutableStateOf(style.background) }
    val ink = remember { Animatable(1f) }
    val stamp = remember { Animatable(1f) }

    LaunchedEffect(status) {
        val target = styleOf(status).background
        if (target == inkColor) return@LaunchedEffect   // primera composición: sin animación
        baseColor = inkColor
        inkColor = target
        if (reduceMotion) {
            ink.snapTo(1f)
            return@LaunchedEffect
        }
        launch {
            ink.snapTo(0f)
            ink.animateTo(1f, tween(340, easing = FastOutSlowInEasing))
            origin = Offset.Unspecified
        }
        // El sello aparece ya grande y rebota al asentarse.
        stamp.snapTo(0.6f)
        stamp.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 600f))
    }

    val borderColor by animateColorAsState(
        style.border,
        animationSpec = if (reduceMotion) tween(0) else tween(250),
        label = "cardBorder",
    )

    Box(
        modifier
            .heightIn(min = 72.dp)
            .clip(CardShape)
            .drawWithCache {
                val maxRadius = hypot(size.width, size.height)
                onDrawBehind {
                    drawRect(baseColor)
                    val center = if (origin.isSpecified) origin else Offset(0f, size.height / 2f)
                    drawCircle(inkColor, radius = maxRadius * ink.value, center = center)
                }
            }
            .border(2.dp, borderColor, CardShape)
            .clickable(
                interactionSource = interactions,
                indication = ripple(color = style.border),
                onClickLabel = "Cambiar estado",
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) {
                contentDescription = student.name
                stateDescription = style.label
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(end = 12.dp)) {
                Text(student.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(student.code, style = MaterialTheme.typography.bodySmall, color = AllinColors.Muted)
            }
            // Sello del estado, ligeramente girado como un sello de registro.
            Text(
                style.label.uppercase(),
                color = style.text,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = stamp.value
                        scaleY = stamp.value
                        rotationZ = -6f
                    }
                    .border(2.dp, style.text, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

private fun today(): String = java.time.LocalDate.now().toString()
