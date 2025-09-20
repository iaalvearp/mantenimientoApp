// Archivo: PreventiveChecklistScreen.kt
package com.alpes.mantenimientoapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreventiveChecklistScreen(
    viewModel: ChecklistViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Checklist Preventivo") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.items) { itemState ->
                ChecklistItem(
                    itemState = itemState,
                    onResponseSelected = { respuesta ->
                        viewModel.onResponseSelected(itemState.actividad.actividad.id, respuesta)
                    },
                    onObservationChanged = { texto ->
                        viewModel.onObservationChanged(itemState.actividad.actividad.id, texto)
                    }
                )
            }
        }
    }
}

@Composable
fun ChecklistItem(
    itemState: ChecklistItemState,
    onResponseSelected: (PosibleRespuesta) -> Unit,
    onObservationChanged: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(),
                        onClick = { isExpanded = !isExpanded }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = itemState.actividad.actividad.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Cerrar" else "Expandir"
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    itemState.actividad.posiblesRespuestas.forEach { respuesta ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                // --- INICIO DE LA NUEVA Y FINAL CORRECCIÓN ---
                                .selectable(
                                    selected = (itemState.respuestaSeleccionada?.id == respuesta.id),
                                    // Añadimos explícitamente la interacción y el efecto visual para máxima compatibilidad
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = rememberRipple(),
                                    onClick = { onResponseSelected(respuesta) }
                                )
                                // --- FIN DE LA NUEVA Y FINAL CORRECCIÓN ---
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (itemState.respuestaSeleccionada?.id == respuesta.id),
                                // El onClick del RadioButton puede ser nulo si el Row ya lo maneja,
                                // pero dejarlo aquí también es seguro.
                                onClick = { onResponseSelected(respuesta) }
                            )
                            Text(text = respuesta.label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }

                    if (itemState.respuestaSeleccionada != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = itemState.observacion,
                            onValueChange = onObservationChanged,
                            label = { Text("Añadir observación...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}