// Archivo: PreventiveChecklistScreen.kt (COMPLETAMENTE RECONSTRUIDO)
package com.alpes.mantenimientoapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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

    LaunchedEffect(key1 = checklistType) {
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
                Icon(Icons.Filled.Save, contentDescription = "Guardar")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Un poco más de espacio
        ) {
            // Lógica para el Firmware en Diagnóstico (sin cambios)
            if (checklistType == "diagnostico") {
                item {
                    Column {
                        // ... (código de firmware se mantiene)
                    }
                }
            }

            // --- LLAMADA AL NUEVO CHECKLISTITEM ---
            items(uiState.items) { itemState ->
                ChecklistItem(
                    itemState = itemState,
                    onSiNoDecision = { decision ->
                        viewModel.onSiNoDecision(itemState.actividad.actividad.id, decision)
                    },
                    onSubRespuestaSelected = { subRespuesta ->
                        viewModel.onSubRespuestaSelected(itemState.actividad.actividad.id, subRespuesta)
                    },
                    onOtrosTextChanged = { texto ->
                        viewModel.onOtrosTextChanged(itemState.actividad.actividad.id, texto)
                    }
                )
            }

            // Campo de Observaciones Generales (sin cambios)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = uiState.observacionGeneral,
                    onValueChange = { viewModel.onGeneralObservationChanged(it) },
                    label = { Text("OBSERVACIONES / RECOMENDACIONES") },
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
            }
        }

        // Diálogo de confirmación (sin cambios)
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


// --- INICIO DEL COMPOSABLE ChecklistItem TOTALMENTE RECONSTRUIDO ---
@Composable
fun ChecklistItem(
    itemState: ChecklistItemState,
    onSiNoDecision: (Boolean) -> Unit,
    onSubRespuestaSelected: (PosibleRespuesta) -> Unit,
    onOtrosTextChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Título de la actividad
            Text(
                text = itemState.actividad.actividad.nombre,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botones "Sí" y "No"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onSiNoDecision(true) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (itemState.decisionSiNo == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) { Text("SÍ") }

                Button(
                    onClick = { onSiNoDecision(false) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (itemState.decisionSiNo == false) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) { Text("NO") }
            }

            AnimatedVisibility(visible = itemState.decisionSiNo != null) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    val subRespuestas = itemState.actividad.posiblesRespuestas.filter {
                        it.esParaRespuestaAfirmativa == itemState.decisionSiNo
                    }

                    subRespuestas.forEach { respuesta ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (itemState.subRespuestaSeleccionada?.id == respuesta.id && itemState.subRespuestaSeleccionada.actividadId == respuesta.actividadId),
                                    onClick = { onSubRespuestaSelected(respuesta) },
                                    role = Role.RadioButton,
                                    // --- LA SOLUCIÓN DEFINITIVA ---
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null // Desactiva el ripple problemático
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (itemState.subRespuestaSeleccionada?.id == respuesta.id && itemState.subRespuestaSeleccionada.actividadId == respuesta.actividadId),
                                onClick = { onSubRespuestaSelected(respuesta) }
                            )
                            Text(text = respuesta.label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }

            AnimatedVisibility(visible = itemState.subRespuestaSeleccionada?.value == "otros") {
                OutlinedTextField(
                    value = itemState.textoOtros,
                    onValueChange = onOtrosTextChanged,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    label = { Text("Por favor, especifique...") }
                )
            }
        }
    }
}
// --- FIN DEL COMPOSABLE ChecklistItem RECONSTRUIDO ---