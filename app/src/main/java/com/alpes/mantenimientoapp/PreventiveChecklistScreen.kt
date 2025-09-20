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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreventiveChecklistScreen(
    equipoId: String, // <-- Recibimos el ID del equipo
    viewModel: ChecklistViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showDialog by viewModel.showSaveConfirmation.collectAsStateWithLifecycle()

    // Estado para controlar qué item está expandido (acordeón exclusivo)
    var expandedItemId by remember { mutableStateOf<Int?>(null) }

    // Efecto para volver atrás cuando el diálogo se cierra
    LaunchedEffect(key1 = Unit) {
        viewModel.loadChecklistData("preventivo")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checklist Preventivo") },
                // Flecha para volver atrás
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        // Botón flotante para guardar
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.saveChecklist(equipoId) }) {
                Icon(Icons.Default.Save, contentDescription = "Guardar")
            }
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
                    isExpanded = expandedItemId == itemState.actividad.actividad.id,
                    onExpand = {
                        expandedItemId = if (expandedItemId == itemState.actividad.actividad.id) {
                            null // Si ya está expandido, lo cerramos
                        } else {
                            itemState.actividad.actividad.id // Si no, lo expandimos
                        }
                    },
                    onResponseSelected = { respuesta ->
                        viewModel.onResponseSelected(itemState.actividad.actividad.id, respuesta)
                    },
                    onObservationChanged = { texto ->
                        viewModel.onObservationChanged(itemState.actividad.actividad.id, texto)
                    }
                )
            }
        }

        // Diálogo de confirmación de guardado
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissSaveConfirmation() },
                title = { Text("Éxito") },
                text = { Text("El mantenimiento se ha guardado correctamente.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.dismissSaveConfirmation()
                            onNavigateBack() // Volvemos a la pantalla anterior
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}

@Composable
fun ChecklistItem(
    itemState: ChecklistItemState,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onResponseSelected: (PosibleRespuesta) -> Unit,
    onObservationChanged: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(),
                        onClick = { onExpand() }
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
                                .selectable(
                                    selected = (itemState.respuestaSeleccionada?.id == respuesta.id),
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = rememberRipple(),
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

                    // --- LÓGICA DE OBSERVACIÓN CORREGIDA ---
                    // El campo de texto solo aparece si la respuesta seleccionada
                    // es "Regular", "Mal" o "Muy Mal".
                    val respuestaSeleccionadaValue = itemState.respuestaSeleccionada?.value
                    val mostrarCampoObservacion = respuestaSeleccionadaValue in listOf("regular", "mal", "muy_mal")

                    if (mostrarCampoObservacion) {
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