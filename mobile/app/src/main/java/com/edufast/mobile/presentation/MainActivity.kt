package com.edufast.mobile.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.edufast.mobile.EduFastApp
import com.edufast.mobile.domain.model.Section

// La pantalla raíz: decide qué mostrar según el estado de la sesión.
// (equivalente a App.tsx en la web)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as EduFastApp).repository

        setContent {
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
                )
                section != null && selectedModule == "attendance" -> AttendanceScreen(
                    repository = repository,
                    section = section!!,
                    onBack = { section = null; selectedModule = null },
                )
                else -> HomeScreen(
                    userName = userName,
                    onModuleSelect = { selectedModule = it }
                )
            }
        }
    }
}