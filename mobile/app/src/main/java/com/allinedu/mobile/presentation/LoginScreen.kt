package com.allinedu.mobile.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.allinedu.mobile.domain.repository.AllinEduRepository
import com.allinedu.mobile.presentation.theme.AllinColors
import com.allinedu.mobile.presentation.theme.ThemeToggleButton
import com.allinedu.mobile.presentation.theme.Wordmark
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Getters: leen la paleta activa, así siguen el cambio de modo oscuro/claro
private val Line: Color get() = AllinColors.Accent
// El paso disponible se enciende en dorado (acento de marca), no en el verde de "asistió"
private val Ready: Color get() = AllinColors.Primary
private val Track: Color get() = AllinColors.Border

// Rebote de "sello": pasa un poco del tamaño final y se asienta
private val PopEasing = CubicBezierEasing(0.34f, 1.8f, 0.5f, 1f)

// Fracción de la línea durante la que cada paso crece
private const val PopLead = 0.12f

private data class ChainStep(val label: String, val ready: Boolean)

private val chainSteps = listOf(
    ChainStep("Planificar", false),
    ChainStep("Aplicar", true),
    ChainStep("Evaluar", false),
    ChainStep("Reforzar", false),
)

// Pantalla de login. Igual que Login.tsx en la web.
@Composable
fun LoginScreen(
    repository: AllinEduRepository,
    onLogin: (userName: String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var errorCount by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var welcomeName by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val reduceMotion = rememberReduceMotion()

    // Sacudida de la tarjeta en cada error
    val shake = remember { Animatable(0f) }
    LaunchedEffect(errorCount) {
        if (errorCount == 0 || reduceMotion) return@LaunchedEffect
        shake.animateTo(0f, keyframes {
            durationMillis = 400
            -8f at 50
            8f at 130
            -8f at 210
            8f at 290
            0f at 400
        })
    }

    // Tras el sello de bienvenida se pasa al menú
    LaunchedEffect(welcomeName) {
        val name = welcomeName ?: return@LaunchedEffect
        delay(if (reduceMotion) 400 else 1100)
        onLogin(name)
    }

    fun submit() {
        if (loading || email.isBlank() || password.isBlank()) return
        scope.launch {
            loading = true
            error = null
            try {
                welcomeName = repository.login(email, password)
            } catch (e: ClientRequestException) {
                // El backend respondió 4xx: las credenciales están mal
                error = "Correo o contraseña incorrectos"
                errorCount++
                loading = false
            } catch (e: Exception) {
                // No hubo respuesta: servidor apagado o sin internet
                error = "Sin conexión con el servidor"
                errorCount++
                loading = false
            }
        }
    }

    Scaffold(containerColor = AllinColors.Background) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(Modifier.widthIn(max = 400.dp).fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                ThemeToggleButton()
            }
            ChainHero(reduceMotion = reduceMotion, modifier = Modifier.widthIn(max = 400.dp))

            Spacer(Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AllinColors.Card),
                border = BorderStroke(1.dp, AllinColors.Border),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .fillMaxWidth()
                    .graphicsLayer { translationX = shake.value.dp.toPx() },
            ) {
                Column(Modifier.padding(24.dp)) {
                    Wordmark(fontSize = 32)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Prepara, aplica y evalúa tus clases con menos carga",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AllinColors.Muted,
                    )

                    Spacer(Modifier.height(20.dp))

                    val name = welcomeName
                    if (name != null) {
                        WelcomeStamp(name = name, reduceMotion = reduceMotion)
                    } else {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo") },
                            singleLine = true,
                            isError = error != null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                            ),
                            colors = fieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            singleLine = true,
                            isError = error != null,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(onDone = { submit() }),
                            trailingIcon = {
                                TextButton(
                                    onClick = { showPassword = !showPassword },
                                    colors = ButtonDefaults.textButtonColors(contentColor = AllinColors.Accent),
                                ) {
                                    Text(if (showPassword) "Ocultar" else "Mostrar")
                                }
                            },
                            colors = fieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        error?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .semantics { liveRegion = LiveRegionMode.Assertive },
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            enabled = !loading,
                            onClick = { submit() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AllinColors.Primary,
                                contentColor = AllinColors.OnPrimary,
                            ),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = AllinColors.OnPrimary,
                                )
                                Spacer(Modifier.width(10.dp))
                                Text("Entrando…")
                            } else {
                                Text("Ingresar", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Campo activo en el color de la marca (no el morado por defecto de Material)
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AllinColors.Accent,
    focusedLabelColor = AllinColors.Accent,
    cursorColor = AllinColors.Accent,
    unfocusedBorderColor = AllinColors.InputBorder,
    unfocusedLabelColor = AllinColors.Muted,
    focusedContainerColor = AllinColors.InputBg,
    unfocusedContainerColor = AllinColors.InputBg,
    focusedTextColor = AllinColors.Text,
    unfocusedTextColor = AllinColors.Text,
)

// La cadena de AllinEdu se dibuja sola: la línea avanza, cada paso aparece cuando
// la línea lo alcanza y "Aplicar" (el eslabón ya construido) se enciende en dorado.
@Composable
private fun ChainHero(reduceMotion: Boolean, modifier: Modifier = Modifier) {
    val line = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val glow = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val pulse = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (reduceMotion) return@LaunchedEffect
        delay(150)
        line.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))
        glow.animateTo(1f, tween(400))
        pulse.animateTo(1f, tween(900, easing = LinearEasing))
    }

    val lastIndex = chainSteps.lastIndex
    // Progreso de aparición de cada paso: empieza a crecer un poco antes de que la
    // línea lo alcance y está completo cuando llega (lectura en fase de dibujo).
    fun stepProgress(i: Int): Float {
        val reached = i / lastIndex.toFloat()
        return ((line.value - reached + PopLead) / PopLead).coerceIn(0f, 1f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "La cadena de AllinEdu: planificar, aplicar en clase, evaluar y reforzar"
            },
    ) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .drawWithCache {
                    val stroke = 3.dp.toPx()
                    val radius = 10.dp.toPx()
                    val cy = size.height / 2f
                    val step = size.width / chainSteps.size
                    val xs = FloatArray(chainSteps.size) { step * (it + 0.5f) }
                    val dotStroke = Stroke(width = stroke)
                    onDrawBehind {
                        drawLine(Track, Offset(xs[0], cy), Offset(xs[lastIndex], cy), stroke, StrokeCap.Round)
                        val end = xs[0] + (xs[lastIndex] - xs[0]) * line.value
                        if (line.value > 0f) {
                            drawLine(Line, Offset(xs[0], cy), Offset(end, cy), stroke, StrokeCap.Round)
                        }
                        for (i in chainSteps.indices) {
                            val p = stepProgress(i)
                            if (p <= 0f) continue
                            val r = radius * PopEasing.transform(p)
                            val center = Offset(xs[i], cy)
                            val ready = chainSteps[i].ready
                            if (ready && pulse.value > 0f && pulse.value < 1f) {
                                drawCircle(
                                    Ready.copy(alpha = 0.45f * (1f - pulse.value)),
                                    radius * (1f + 1.4f * pulse.value),
                                    center,
                                )
                            }
                            val ring = if (ready) lerp(Line, Ready, glow.value) else Line
                            val fill = if (ready) lerp(AllinColors.Card, Ready, glow.value) else AllinColors.Card
                            drawCircle(fill, r, center)
                            drawCircle(ring, r, center, style = dotStroke)
                        }
                    }
                },
        )
        Row(Modifier.fillMaxWidth()) {
            chainSteps.forEachIndexed { i, s ->
                Text(
                    s.label,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (s.ready) FontWeight.Bold else FontWeight.Medium,
                    color = if (s.ready) AllinColors.GoldText else AllinColors.Muted,
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer { alpha = stepProgress(i) },
                )
            }
        }
    }
}

// Sello de bienvenida, el mismo lenguaje visual que los sellos de asistencia.
@Composable
private fun WelcomeStamp(name: String, reduceMotion: Boolean) {
    val scale = remember { Animatable(if (reduceMotion) 1f else 0.6f) }
    LaunchedEffect(Unit) {
        if (!reduceMotion) scale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 600f))
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
    ) {
        Text(
            "¡BIENVENIDO, PROFE!",
            color = AllinColors.GoldText,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    rotationZ = -6f
                }
                .border(3.dp, AllinColors.Primary, RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(name, color = AllinColors.Muted, fontWeight = FontWeight.SemiBold)
    }
}
