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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreventiveChecklistScreen(
    equipoId: String,
    viewModel: ChecklistViewModel,
    onNavigateBack: () -> Unit,
    title: String,
    checklistType: String
)  {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showDialog by viewModel.showSaveConfirmation.collectAsStateWithLifecycle()

    // Estado para controlar qué item está expandido (acordeón exclusivo)
    var expandedItemId by remember { mutableStateOf<Int?>(null) }

    // Efecto para volver atrás cuando el diálogo se cierra
    LaunchedEffect(key1 = checklistType) { // Parámetro 'checklistType' AHORA SÍ SE USA
        viewModel.loadChecklistData(checklistType)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
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
                    // Le decimos al item si DEBE estar expandido
                    isExpanded = expandedItemId == itemState.actividad.actividad.id,
                    // --- PUNTO CLAVE 2: La orden ---
                    // Le damos al item la orden de qué hacer cuando se le dé clic
                    onExpand = {
                        expandedItemId = if (expandedItemId == itemState.actividad.actividad.id) {
                            null // Si ya está abierto, ciérralo
                        } else {
                            itemState.actividad.actividad.id // Si está cerrado, ábrelo
                        }
                    },
                    onResponseSelected = { respuesta ->
                        viewModel.onResponseSelected(
                            actividadId = itemState.actividad.actividad.id,
                            respuesta = respuesta
                        )
                    },
                    onObservationChanged = { texto ->
                        viewModel.onObservationChanged(itemState.actividad.actividad.id, texto)
                    }
                )
            }

            // Campo de Observaciones Generales
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.observacionGeneral,
                    onValueChange = { viewModel.onGeneralObservationChanged(it) },
                    label = { Text("OBSERVACIONES / RECOMENDACIONES") },
                    modifier = Modifier.fillMaxWidth().height(150.dp)
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
                            onNavigateBack()
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}

// DENTRO DE PreventiveChecklistScreen.kt

@Composable
fun ChecklistItem(
    itemState: ChecklistItemState,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onResponseSelected: (PosibleRespuesta) -> Unit,
    onObservationChanged: (String) -> Unit
) {
    val esSeleccionMultiple = itemState.actividad.actividad.tipoSeleccion == "multiple_choice"

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(),
                        onClick = onExpand
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

            // --- INICIO DEL CÓDIGO COMPLETO Y CORREGIDO ---
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    itemState.actividad.posiblesRespuestas.forEach { respuesta ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = itemState.respuestasSeleccionadas.contains(respuesta),
                                    // Esta es la corrección que faltaba para las opciones internas
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = rememberRipple(),
                                    onClick = { onResponseSelected(respuesta) }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (esSeleccionMultiple) {
                                Checkbox(
                                    checked = itemState.respuestasSeleccionadas.contains(respuesta),
                                    onCheckedChange = { onResponseSelected(respuesta) }
                                )
                            } else {
                                RadioButton(
                                    selected = itemState.respuestasSeleccionadas.contains(respuesta),
                                    onClick = { onResponseSelected(respuesta) }
                                )
                            }
                            Text(text = respuesta.label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }

                    // Lógica para la observación individual
                    val respuestaValue = itemState.respuestasSeleccionadas.firstOrNull()?.value
                    val mostrarObsIndividual = respuestaValue in listOf("regular", "mal", "muy_mal")

                    if (mostrarObsIndividual) {
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
            // --- FIN DEL CÓDIGO COMPLETO ---
        }
    }
}