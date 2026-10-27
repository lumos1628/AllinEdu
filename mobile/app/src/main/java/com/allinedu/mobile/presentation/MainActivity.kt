package com.allinedu.mobile.presentation

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.allinedu.mobile.AllinEduApp
import com.allinedu.mobile.domain.model.Section
import com.allinedu.mobile.presentation.theme.AllinColors
import com.allinedu.mobile.presentation.theme.AllinEduTheme
import com.allinedu.mobile.presentation.theme.loadSavedTheme

// La pantalla raíz: decide qué mostrar según el estado de la sesión.
// (equivalente a App.tsx en la web)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as AllinEduApp).repository
        loadSavedTheme(this)

        setContent { AllinEduTheme {
            // Íconos de la barra de estado claros en modo oscuro y oscuros en modo claro
            val dark = AllinColors.palette.isDark
            LaunchedEffect(dark) {
                val style = if (dark) SystemBarStyle.dark(Color.TRANSPARENT)
                else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }

            var loggedIn by remember { mutableStateOf(false) }
            var userName by remember { mutableStateOf("Profesor") }
            var section by remember { mutableStateOf<Section?>(null) }
            var selectedModule by remember { mutableStateOf<String?>(null) }

            when {
                !loggedIn -> LoginScreen(
                    repository = repository,
                    onLogin = { name ->
                        userName = name
                        loggedIn = true
                    },
                )
                selectedModule == null -> HomeScreen(
                    userName = userName,
                    onModuleSelect = { module ->
                        selectedModule = module
                        when (module) {
                            "attendance" -> {} // Irá a AttendanceScreen
                            "planning" -> {} // Próximamente
                            "evaluation" -> {} // Próximamente
                            "reinforcement" -> {} // Próximamente
                            "communication" -> {} // Próximamente
                        }
                    }
                )
                section == null && selectedModule == "attendance" -> SectionListScreen(
                    repository = repository,
                    onSelect = { section = it },
                    onBack = { selectedModule = null },
                )
                section != null && selectedModule == "attendance" -> AttendanceScreen(
                    repository = repository,
                    section = section!!,
                    onBack = { section = null },
                )
                else -> HomeScreen(
                    userName = userName,
                    onModuleSelect = { selectedModule = it }
                )
            }
        } }
    }
}