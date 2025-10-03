// Archivo: PreventiveChecklistScreen.kt (COMPLETAMENTE RECONSTRUIDO)
package com.alpes.mantenimientoapp


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
    // --- NUEVO: Estado para controlar qué item está expandido (acordeón) ---
    var expandedItemId by remember { mutableStateOf<Int?>(null) }

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
                val actividadId = itemState.actividad.actividad.id
                ChecklistItem(
                    itemState = itemState,
                    // --- NUEVOS PARÁMETROS PARA EL ACORDEÓN ---
                    isExpanded = expandedItemId == actividadId,
                    onExpand = {
                        expandedItemId = if (expandedItemId == actividadId) null else actividadId
                    },
                    // --- El resto de parámetros se mantienen ---
                    onSiNoDecision = { decision ->
                        viewModel.onSiNoDecision(actividadId, decision)
                    },
                    onSubRespuestaSelected = { subRespuesta ->
                        viewModel.onSubRespuestaSelected(actividadId, subRespuesta)
                    },
                    onOtrosTextChanged = { texto ->
                        viewModel.onOtrosTextChanged(actividadId, texto)
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
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onSiNoDecision: (Boolean) -> Unit,
    onSubRespuestaSelected: (PosibleRespuesta) -> Unit,
    onOtrosTextChanged: (String) -> Unit
) {
    // --- NUEVO: Definimos los colores para reutilizarlos ---
    val colorNaranja = Color(0xFFF57C00)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Fila del Título (con el clickable corregido)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onExpand
                    )
                    .padding(16.dp),
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

            // Contenido expandible
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Botones "Sí" y "No" CON COLORES ACTUALIZADOS ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onSiNoDecision(true) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (itemState.decisionSiNo == true) colorNaranja else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (itemState.decisionSiNo == true) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) { Text("SÍ") }

                        Button(
                            onClick = { onSiNoDecision(false) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (itemState.decisionSiNo == false) colorNaranja else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (itemState.decisionSiNo == false) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) { Text("NO") }
                    }

                    // El resto del código no cambia...
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
                                            selected = (itemState.subRespuestaSeleccionada?.dbId == respuesta.dbId),
                                            onClick = { onSubRespuestaSelected(respuesta) },
                                            role = Role.RadioButton,
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        )
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (itemState.subRespuestaSeleccionada?.dbId == respuesta.dbId),
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
    }
}