// Archivo: PreventiveChecklistScreen.kt (REEMPLAZAR COMPLETO)
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
import androidx.compose.ui.text.font.FontWeight
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

    var expandedItemId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(key1 = checklistType, key2 = equipoId) {
        viewModel.loadChecklistData(checklistType, equipoId)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- DIAGNÓSTICO: Nueva sección para el Firmware ---
            if (checklistType == "diagnostico") {
                item {
                    Column {
                        Text(
                            "ESTADO DEL FIRMWARE DEL EQUIPO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = uiState.versionFirmwareActual,
                            onValueChange = { viewModel.onVersionActualChanged(it) },
                            label = { Text("Versión actual de Firmware / Parche") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.versionFirmwareDespues,
                            onValueChange = { viewModel.onVersionDespuesChanged(it) },
                            label = { Text("Versión después de actualización") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                    }
                }
            }

            // --- LLAMADA AL NUEVO CHECKLISTITEM ---
            items(uiState.items) { itemState ->
                val actividadId = itemState.actividad.actividad.dbId
                ChecklistItem(
                    itemState = itemState,
                    isExpanded = expandedItemId == actividadId,
                    onExpand = {
                        expandedItemId = if (expandedItemId == actividadId) null else actividadId
                    },
                    checklistType = checklistType,
                    onSiNoDecision = { decision -> viewModel.onSiNoDecision(actividadId, decision) },
                    onSubRespuestaSelected = { subRespuesta -> viewModel.onSubRespuestaSelected(actividadId, subRespuesta) },
                    onOtrosTextChanged = { texto -> viewModel.onOtrosTextChanged(actividadId, texto) },
                    onDiagnosticoCheckedChange = { isChecked -> viewModel.onDiagnosticoCheckedChange(actividadId, isChecked) }
                )
            }

            // Campo de Observaciones Generales (sin cambios)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                // Mostramos el campo de Mantenimiento
                if (checklistType == "preventivo" || checklistType == "correctivo") {
                    OutlinedTextField(
                        value = uiState.observacionMantenimiento,
                        onValueChange = { viewModel.onMantenimientoObservationChanged(it) },
                        label = { Text("OBSERVACIONES / RECOMENDACIONES (Mantenimiento)") },
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                }
                // Mostramos el campo de Diagnóstico
                else if (checklistType == "diagnostico") {
                    OutlinedTextField(
                        value = uiState.observacionDiagnostico,
                        onValueChange = { viewModel.onDiagnosticoObservationChanged(it) },
                        label = { Text("OBSERVACIONES / RECOMENDACIONES (Diagnóstico)") },
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                }
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

        // --- CAMBIO #1: Diálogo de validación de Diagnóstico (Actualizado) ---
        if (uiState.mostrarDialogoValidacionDiagnostico) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDiagnosticValidationDialog() }, // <-- Función renombrada
                title = { Text("Tareas Incompletas") },
                text = {
                    Column {
                        Text("Por favor, complete las siguientes tareas de diagnóstico antes de guardar:")
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.tareasNoCompletadas.forEach {
                            Text("• $it")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissDiagnosticValidationDialog() }) { // <-- Función renombrada
                        Text("Aceptar")
                    }
                }
            )
        }

        // --- CAMBIO #2: ¡NUEVO DIÁLOGO DE VALIDACIÓN GENÉRICA! ---
        if (uiState.validationError != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissGenericValidationError() },
                title = { Text("Formulario Incompleto") },
                // Aseguramos que el texto muestre saltos de línea
                text = { Text(uiState.validationError!!) },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissGenericValidationError() }) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}


// --- COMPOSABLE ChecklistItem (REEMPLAZAR COMPLETO) ---
// (Este es el mismo código de la respuesta anterior, que corrige la lógica del correctivo)
@Composable
fun ChecklistItem(
    itemState: ChecklistItemState,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    checklistType: String, // "preventivo", "correctivo", "diagnostico"
    onSiNoDecision: (Boolean) -> Unit,
    onSubRespuestaSelected: (PosibleRespuesta) -> Unit,
    onOtrosTextChanged: (String) -> Unit,
    onDiagnosticoCheckedChange: (Boolean) -> Unit
) {
    // Si el checklist es de tipo "diagnostico", mostramos la UI simple
    if (checklistType == "diagnostico") {
        // Caso especial para el item con sub-opciones (RadioButtons)
        if (itemState.actividad.posiblesRespuestas.size > 1) {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = itemState.actividad.actividad.nombre, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    itemState.actividad.posiblesRespuestas.forEach { respuesta ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = itemState.subRespuestaSeleccionada?.dbId == respuesta.dbId,
                                    onClick = { onSubRespuestaSelected(respuesta) },
                                    role = Role.RadioButton,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = itemState.subRespuestaSeleccionada?.dbId == respuesta.dbId,
                                onClick = { onSubRespuestaSelected(respuesta) }
                            )
                            Text(text = respuesta.label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        } else {
            // Para el resto de items de diagnóstico, un simple checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = itemState.isChecked,
                        onClick = { onDiagnosticoCheckedChange(!itemState.isChecked) },
                        role = Role.Checkbox,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = itemState.isChecked,
                    onCheckedChange = { onDiagnosticoCheckedChange(it) }
                )
                Text(
                    text = itemState.actividad.actividad.nombre,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    } else {
        // --- VISTA PARA PREVENTIVO/CORRECTIVO ---
        val colorNaranja = Color(0xFFF57C00)
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // Título expandible (Sin cambios)
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

                        // Botones SÍ/NO (Sin cambios)
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

                        // Lógica para mostrar sub-respuestas (Preventivo) o campo de texto (Correctivo)
                        AnimatedVisibility(visible = itemState.decisionSiNo != null) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))

                                // SI ES PREVENTIVO, mostramos las sub-respuestas (RadioButtons)
                                if (checklistType == "preventivo") {
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

                                    // Campo "Otros" del PREVENTIVO (solo si se selecciona "otros")
                                    AnimatedVisibility(visible = itemState.subRespuestaSeleccionada?.value == "otros") {
                                        OutlinedTextField(
                                            value = itemState.textoOtros,
                                            onValueChange = onOtrosTextChanged,
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                            label = { Text("Por favor, especifique...") }
                                        )
                                    }
                                }
                                // SI ES CORRECTIVO, mostramos el campo de texto directamente
                                else if (checklistType == "correctivo") {
                                    OutlinedTextField(
                                        value = itemState.textoOtros,
                                        onValueChange = onOtrosTextChanged,
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                        label = { Text("Ingresar causa...") } // Requisito #7
                                    )
                                }
                            }
                        } // Fin AnimatedVisibility (decisionSiNo != null)
                    }
                } // Fin AnimatedVisibility (isExpanded)
            }
        } // Fin Card
    } // Fin Else
}