// Archivo: PreventiveChecklistScreen.kt
package com.alpes.mantenimientoapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
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
            Text(
                text = itemState.actividad.actividad.nombre,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
            )

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    itemState.actividad.posiblesRespuestas.forEach { respuesta ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (itemState.respuestaSeleccionada?.id == respuesta.id),
                                    onClick = { onResponseSelected(respuesta) }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (itemState.respuestaSeleccionada?.id == respuesta.id),
                                onClick = { onResponseSelected(respuesta) }
                            )
                            Text(text = respuesta.label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }

                    // El campo de observación aparece si se ha seleccionado cualquier respuesta
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