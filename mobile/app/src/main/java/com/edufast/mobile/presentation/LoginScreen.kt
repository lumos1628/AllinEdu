package com.edufast.mobile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufast.mobile.domain.repository.EduFastRepository
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.launch

// Pantalla de login. Igual que Login.tsx en la web.
@Composable
fun LoginScreen(
    repository: EduFastRepository,
    onLogin: (userName: String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("EduFast", style = MaterialTheme.typography.headlineLarge)
            Text("Asistencia para docentes", style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
            )

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Spacer(Modifier.height(12.dp))

            Button(
                enabled = !loading,
                onClick = {
                    scope.launch {
                        loading = true
                        error = null
                        try {
                            repository.login(email, password)
                            onLogin(email.substringBefore("@"))
                        } catch (e: ClientRequestException) {
                            // El backend respondió 4xx: las credenciales están mal
                            error = "Credenciales inválidas"
                        } catch (e: Exception) {
                            // No hubo respuesta: servidor apagado o sin internet
                            error = "Sin conexión con el servidor"
                        } finally {
                            loading = false
                        }
                    }
                },
            ) {
                Text(if (loading) "Entrando..." else "Ingresar")
            }
        }
    }
}