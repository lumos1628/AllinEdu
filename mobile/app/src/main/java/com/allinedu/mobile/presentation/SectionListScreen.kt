package com.allinedu.mobile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.allinedu.mobile.domain.model.Section
import com.allinedu.mobile.domain.repository.AllinEduRepository
import com.allinedu.mobile.presentation.theme.AllinColors

// Pantalla de secciones. Igual que SectionList.tsx en la web.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionListScreen(
    repository: AllinEduRepository,
    onSelect: (Section) -> Unit,
    onBack: () -> Unit,
) {
    var sections by remember { mutableStateOf<List<Section>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            sections = repository.getMySections()
        } catch (e: Exception) {
            error = "No se pudieron cargar tus secciones. Revisa la conexión."
        } finally {
            loading = false
        }
    }

    Scaffold(
        containerColor = AllinColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("Mis secciones", color = AllinColors.Text, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver al inicio")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AllinColors.Card),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(
                "Elige una sección para tomar la asistencia de hoy.",
                style = MaterialTheme.typography.bodyMedium,
                color = AllinColors.Muted,
            )
            Spacer(Modifier.height(12.dp))

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                error == null && sections.isEmpty() -> Text(
                    "Aún no tienes secciones asignadas. Consulta con la dirección de tu institución.",
                    color = AllinColors.Muted,
                )
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(sections, key = { it.id }) { section ->
                        SectionCard(section = section, onClick = { onSelect(section) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(section: Section, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AllinColors.Card),
        border = BorderStroke(1.dp, AllinColors.Border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
            // Filo dorado a la izquierda, como en las tarjetas de la web
            Box(Modifier.width(6.dp).fillMaxHeight().background(AllinColors.Primary))
            Column(Modifier.weight(1f).padding(16.dp)) {
                Text(section.grado, style = MaterialTheme.typography.bodySmall, color = AllinColors.Muted)
                Text(
                    "Sección ${section.nombre}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AllinColors.Text,
                )
                Text(
                    "Tomar asistencia",
                    style = MaterialTheme.typography.labelLarge,
                    color = AllinColors.GoldText,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AllinColors.Primary,
                modifier = Modifier.padding(end = 12.dp),
            )
        }
    }
}
