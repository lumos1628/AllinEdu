# Diseño del Menú Principal — AllinEdu Mobile

**Archivo:** `mobile/app/src/main/java/com/edufast/mobile/presentation/HomeScreen.kt`

---

## 🎨 Vista General

El menú es un **dashboard elegante** con Material Design 3 que muestra la cadena pedagógica en 5 módulos:

```
┌─────────────────────────────────────────┐
│  Hola, Maria García      [⚙️]           │  ← TopAppBar
│  AllinEdu                                │
└─────────────────────────────────────────┘

  Planificar → Aplicar → Evaluar → Reforzar → Comunicar

┌──────────────────────────────────────────┐
│ ┌─────────────────┐ ┌─────────────────┐ │
│ │  ✓             │ │  ✏️             │ │
│ │ ASISTENCIA     │ │ PLANIFICACIÓN   │ │
│ │ Registra...    │ │ Prepara...      │ │
│ └─────────────────┘ └─────────────────┘ │
│                                          │
│ ┌─────────────────┐ ┌─────────────────┐ │
│ │  ℹ️             │ │  🔧             │ │
│ │ EVALUACIÓN     │ │ REFUERZO        │ │
│ │ Próximamente   │ │ Próximamente    │ │
│ └─────────────────┘ └─────────────────┘ │
│                                          │
│ ┌─────────────────┐                     │
│ │  🔔             │                     │
│ │ COMUNICACIÓN    │                     │
│ │ Próximamente    │                     │
│ └─────────────────┘                     │
└──────────────────────────────────────────┘
```

---

## 🎯 Características del Diseño

### 1. **Grid de 2 columnas (+ 1 en fila 3)**
   - Tarjetas responsivas
   - Espaciado de 12dp entre tarjetas
   - Altura: 180dp

### 2. **Cada Tarjeta Contiene**
   - **Ícono grande** (48x48dp) con fondo de color
   - **Título** (titleMedium, SemiBold)
   - **Descripción** (bodySmall, 2 líneas máximo)
   - **Badge** "Próximamente" si está deshabilitado
   - **Elevación animada** al presionar

### 3. **Colores (Material 3)**
   | Módulo | Color | Hex |
   |--------|-------|-----|
   | Asistencia | Verde | `#4CAF50` |
   | Planificación | Azul | `#2196F3` |
   | Evaluación | Naranja | `#FF9800` |
   | Refuerzo | Púrpura | `#9C27B0` |
   | Comunicación | Rojo | `#F44336` |

### 4. **Efectos**
   - **Animación de contenido** cuando se presiona (elevation 4dp → 8dp)
   - **Color de fondo dinámico**: 8% opacidad del color del módulo
   - **Estados deshabilitados**: opacidad 50% + badge

### 5. **TopAppBar Personalizado**
   - Saludo: "Hola, {nombre}"
   - Subtítulo: "AllinEdu"
   - Botón Settings (por implementar)

---

## 📁 Estructura del Código

```kotlin
// Data class para cada módulo
data class ModuleItem(
    val id: String,                  // "attendance", "planning", etc.
    val title: String,               // "Asistencia"
    val description: String,         // "Registra y consulta..."
    val icon: ImageVector,           // Icons.Default.CheckCircle
    val color: Color,                // Color(0xFF4CAF50)
    val isEnabled: Boolean = true    // false = "Próximamente"
)

// Función principal
@Composable
fun HomeScreen(
    userName: String = "Profesor",
    onModuleSelect: (String) -> Unit = {}
)

// Funciones auxiliares
@Composable
private fun HomeTopAppBar(userName: String)

@Composable
private fun ModuleCard(
    module: ModuleItem,
    onClick: () -> Unit
)
```

---

## 🔌 Cómo Integrar en MainActivity

**Estado actual (AHORA):**
```kotlin
LoginScreen → HomeScreen (NUEVO) → SectionListScreen → AttendanceScreen
```

**En MainActivity.kt:**
```kotlin
var selectedModule by remember { mutableStateOf<String?>(null) }

when {
    !loggedIn -> LoginScreen(...)
    selectedModule == null -> HomeScreen(
        userName = userName,
        onModuleSelect = { module ->
            selectedModule = module
            // Navegar según el módulo
            when (module) {
                "attendance" -> {}      // Va a SectionListScreen
                "planning" -> {}        // Próximamente
                "evaluation" -> {}      // Próximamente
                "reinforcement" -> {}   // Próximamente
                "communication" -> {}   // Próximamente
            }
        }
    )
    // ... resto de pantallas
}
```

---

## ✨ Próximos Pasos

### Semana 5 (Navigation Compose)
Reemplazar este sistema de estado por Navigation Compose:
```kotlin
NavHost(navController, startDestination = Routes.HOME) {
    composable(Routes.HOME) { HomeScreen(onModuleSelect = { navController.navigate(...) }) }
    composable(Routes.ATTENDANCE) { SectionListScreen(...) }
    composable(Routes.PLANNING) { PlanningScreen(...) }
    // ... etc
}
```

### Fase Siguiente
Implementar las pantallas deshabilitadas:
- `PlanningScreen` (Planificación)
- `EvaluationScreen` (Evaluación)
- `ReinforcementScreen` (Refuerzo)
- `CommunicationScreen` (Comunicación)

---

## 🎨 Personalización

### Cambiar Colores
```kotlin
ModuleItem(
    color = Color(0xFFTuHexColor),  // Reemplaza con tu color
)
```

### Cambiar Ícono
```kotlin
ModuleItem(
    icon = Icons.Default.OtroIcon,  // Cambia el ícono
)
```

### Agregar Nuevo Módulo
```kotlin
ModuleItem(
    id = "newModule",
    title = "Mi Módulo",
    description = "Descripción aquí",
    icon = Icons.Default.SomeIcon,
    color = Color(0xFF...),
    isEnabled = false  // Mientras se desarrolla
)
```

---

## 📱 Cómo Se Ve en Diferentes Dispositivos

### Teléfono (375x812 dp)
- 2 columnas, 5 filas
- Scroll vertical

### Tablet (768x1024 dp)
- 3 columnas, 2 filas
- (Mejora futura: usar `GridCells.Adaptive(minSize = 200.dp)`)

---

## ✅ Checklist de Verificación

- [x] Compilación sin errores
- [x] Menú se muestra después del login
- [x] Colores Material Design 3
- [x] Iconos visibles y legibles
- [x] "Próximamente" en módulos deshabilitados
- [x] Animación de elevación al presionar
- [x] Responsive grid
- [ ] Navegación real (Semana 5, Navigation Compose)
- [ ] Implementar módulos deshabilitados
- [ ] Agregar preferencias/settings

---

**Archivo:** [HomeScreen.kt](mobile/app/src/main/java/com/edufast/mobile/presentation/HomeScreen.kt)

**Última actualización:** 2026-10-12
