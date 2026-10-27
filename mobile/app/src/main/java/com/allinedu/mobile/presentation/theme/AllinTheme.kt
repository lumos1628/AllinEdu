package com.allinedu.mobile.presentation.theme

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Identidad de AllinEdu (ver IDENTIDAD.md): modo oscuro por defecto, dorado como color de
// acción y azul como complemento. Son los mismos valores que los tokens de web/src/App.css.
data class AllinPalette(
    val isDark: Boolean,
    val background: Color,
    val card: Color,
    val inputBg: Color,
    val text: Color,
    val muted: Color,
    val border: Color,
    val rail: Color,
    val inputBorder: Color,      // borde de campos: contraste 3:1
    val primary: Color,          // dorado: botones, paso disponible, logotipo
    val onPrimary: Color,        // texto sobre dorado
    val goldText: Color,         // dorado usado como texto
    val accent: Color,           // azul complementario: enlaces, foco, línea de la cadena
    // Estados de asistencia: semánticos, no son colores de marca
    val presentBg: Color, val present: Color, val presentText: Color,
    val lateBg: Color, val late: Color, val lateText: Color,
    val absentBg: Color, val absent: Color, val absentText: Color,
)

val DarkPalette = AllinPalette(
    isDark = true,
    background = Color(0xFF0E0F12),
    card = Color(0xFF17191E),
    inputBg = Color(0xFF121418),
    text = Color(0xFFF2EFE8),
    muted = Color(0xFFA3A7B1),
    border = Color(0xFF2A2E37),
    rail = Color(0xFF3A3F4B),
    inputBorder = Color(0xFF5F6676),
    primary = Color(0xFFE3B341),
    onPrimary = Color(0xFF16130A),
    goldText = Color(0xFFE3B341),
    accent = Color(0xFF7AA2FF),
    presentBg = Color(0xFF10291C), present = Color(0xFF2F9E5E), presentText = Color(0xFF6EE7A0),
    lateBg = Color(0xFF2A2210), late = Color(0xFFB8890F), lateText = Color(0xFFF2C14E),
    absentBg = Color(0xFF2E1416), absent = Color(0xFFC93B3B), absentText = Color(0xFFF58A8A),
)

// Modo claro opcional, pensado para aulas con mucha luz
val LightPalette = AllinPalette(
    isDark = false,
    background = Color(0xFFF7F5F0),
    card = Color(0xFFFFFFFF),
    inputBg = Color(0xFFFFFFFF),
    text = Color(0xFF1B1D22),
    muted = Color(0xFF5F6470),
    border = Color(0xFFE4E1D8),
    rail = Color(0xFFD3CFC4),
    inputBorder = Color(0xFF8C8F98),
    primary = Color(0xFFE3B341),
    onPrimary = Color(0xFF16130A),
    goldText = Color(0xFF85650C),   // el dorado puro no se lee como texto sobre claro
    accent = Color(0xFF2F5BD3),
    presentBg = Color(0xFFDCFCE7), present = Color(0xFF16A34A), presentText = Color(0xFF15803D),
    lateBg = Color(0xFFFEF9C3), late = Color(0xFFCA8A04), lateText = Color(0xFF854D0E),
    absentBg = Color(0xFFFEE2E2), absent = Color(0xFFDC2626), absentText = Color(0xFFB91C1C),
)

// Paleta activa. Es estado observable: al cambiarla, Compose redibuja todo lo que la lee,
// también dentro de lambdas de dibujo (drawWithCache), sin pasar la paleta por parámetros.
object AllinColors {
    var palette by mutableStateOf(DarkPalette)

    val Background get() = palette.background
    val Card get() = palette.card
    val InputBg get() = palette.inputBg
    val Text get() = palette.text
    val Muted get() = palette.muted
    val Border get() = palette.border
    val Rail get() = palette.rail
    val InputBorder get() = palette.inputBorder
    val Primary get() = palette.primary
    val OnPrimary get() = palette.onPrimary
    val GoldText get() = palette.goldText
    val Accent get() = palette.accent
    val PresentBg get() = palette.presentBg
    val Present get() = palette.present
    val PresentText get() = palette.presentText
    val LateBg get() = palette.lateBg
    val Late get() = palette.late
    val LateText get() = palette.lateText
    val AbsentBg get() = palette.absentBg
    val Absent get() = palette.absent
    val AbsentText get() = palette.absentText
}

private const val PREFS = "allinedu"
private const val KEY_THEME = "theme"

// Carga el modo guardado (oscuro si no hay nada guardado)
fun loadSavedTheme(context: Context) {
    val light = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_THEME, "dark") == "light"
    AllinColors.palette = if (light) LightPalette else DarkPalette
}

fun toggleTheme(context: Context) {
    val next = if (AllinColors.palette.isDark) LightPalette else DarkPalette
    AllinColors.palette = next
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        .putString(KEY_THEME, if (next.isDark) "dark" else "light")
        .apply()
}

@Composable
fun AllinEduTheme(content: @Composable () -> Unit) {
    val p = AllinColors.palette
    val scheme = if (p.isDark) {
        darkColorScheme(
            primary = p.primary, onPrimary = p.onPrimary,
            secondary = p.accent, onSecondary = p.background,
            background = p.background, onBackground = p.text,
            surface = p.card, onSurface = p.text,
            surfaceVariant = p.card, onSurfaceVariant = p.muted,
            surfaceContainerLowest = p.card, surfaceContainerLow = p.card, surfaceContainer = p.card,
            surfaceContainerHigh = p.card, surfaceContainerHighest = p.card,
            primaryContainer = p.primary, onPrimaryContainer = p.onPrimary,
            outline = p.inputBorder, outlineVariant = p.border,
            error = p.absentText,
        )
    } else {
        lightColorScheme(
            primary = p.primary, onPrimary = p.onPrimary,
            secondary = p.accent, onSecondary = Color.White,
            background = p.background, onBackground = p.text,
            surface = p.card, onSurface = p.text,
            surfaceVariant = p.background, onSurfaceVariant = p.muted,
            surfaceContainerLowest = p.card, surfaceContainerLow = p.card, surfaceContainer = p.card,
            surfaceContainerHigh = p.card, surfaceContainerHighest = p.card,
            primaryContainer = p.primary, onPrimaryContainer = p.onPrimary,
            outline = p.inputBorder, outlineVariant = p.border,
            error = p.absentText,
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

// Botón ☀/☾ para alternar entre modo oscuro y claro
@Composable
fun ThemeToggleButton() {
    val context = LocalContext.current
    val dark = AllinColors.palette.isDark
    IconButton(
        onClick = { toggleTheme(context) },
        modifier = Modifier.semantics {
            contentDescription = if (dark) "Cambiar a modo claro" else "Cambiar a modo oscuro"
        },
    ) {
        // \uFE0E fuerza el glifo de texto (si no, Android lo pinta como emoji y no toma el color)
        Text(if (dark) "\u2600\uFE0E" else "\u263E\uFE0E", color = AllinColors.GoldText, fontSize = 22.sp)
    }
}

// Logotipo: "Allin" en el color del texto, "Edu" en dorado y una barra dorada debajo.
@Composable
fun Wordmark(modifier: Modifier = Modifier, fontSize: Int = 28) {
    Column(modifier.semantics(mergeDescendants = true) { contentDescription = "AllinEdu" }) {
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = AllinColors.Text)) { append("Allin") }
                withStyle(SpanStyle(color = AllinColors.GoldText)) { append("Edu") }
            },
            style = TextStyle(fontSize = fontSize.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
        )
        Spacer(
            Modifier
                .padding(top = 2.dp)
                .width((fontSize * 1.1f).dp)
                .height((fontSize / 7f).coerceAtLeast(3f).dp)
                .background(AllinColors.Primary, RoundedCornerShape(50)),
        )
    }
}
