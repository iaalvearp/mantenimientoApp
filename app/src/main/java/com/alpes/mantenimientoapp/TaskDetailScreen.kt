// Archivo: TaskDetailScreen.kt
package com.alpes.mantenimientoapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    equipoId: String,
    numeroSerie: String,
    viewModel: TaskDetailViewModel,
    onNavigateBack: () -> Unit,
    onNextClicked: () -> Unit
) {
    LaunchedEffect(key1 = equipoId) {
        viewModel.loadDataForEquipo(equipoId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ficha de Mantenimiento") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("INFORMACIÓN GENERAL", style = MaterialTheme.typography.titleMedium)

            SearchableDropdown(label = "Cliente", options = uiState.allClientes, selectedOption = uiState.clienteSeleccionado, onOptionSelected = { viewModel.onClienteSelected(it) }, optionToString = { it.nombreCompleto }, isReadOnly = true)
            SearchableDropdown(label = "Nombre del proyecto", options = uiState.proyectosFiltrados, selectedOption = uiState.proyectoSeleccionado, onOptionSelected = { viewModel.onProyectoSelected(it) }, optionToString = { it.nombre }, enabled = uiState.clienteSeleccionado != null, isReadOnly = true)
            SearchableDropdown(label = "Provincia", options = uiState.allProvincias, selectedOption = uiState.provinciaSeleccionada, onOptionSelected = { viewModel.onProvinciaSelected(it) }, optionToString = { it.nombre }, isReadOnly = true)
            SearchableDropdown(label = "Ciudad", options = uiState.ciudadesFiltradas, selectedOption = uiState.ciudadSeleccionada, onOptionSelected = { viewModel.onCiudadSelected(it) }, optionToString = { it.nombre }, enabled = uiState.provinciaSeleccionada != null, isReadOnly = true)

            // Este es un campo de texto simple que se auto-rellena
            SimpleDropdown(
                label = "Unidad de Negocio",
                options = uiState.unidadesNegocioFiltradas,
                selectedOption = uiState.unidadNegocioSeleccionada,
                onOptionSelected = { viewModel.onUnidadNegocioSelected(it) },
                optionToString = { it.nombre },
                enabled = uiState.ciudadSeleccionada != null
            )

            // Este SÍ es el buscador con filtro
            SearchableDropdown(
                label = "Localidad, oficina, subestación",
                options = uiState.agenciasFiltradas,
                selectedOption = uiState.agenciaSeleccionada,
                onOptionSelected = { viewModel.onAgenciaSelected(it) },
                optionToString = { it.nombre },
                enabled = uiState.ciudadSeleccionada != null,
                searchText = uiState.agenciaSearchText,
                onSearchTextChanged = { viewModel.onAgenciaSearchTextChanged(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("INFORMACIÓN DE EQUIPO", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = uiState.equipo?.id ?: "Cargando...",
                onValueChange = {},
                readOnly = true,
                label = { Text("Número de Serie (ID)") },
                modifier = Modifier.fillMaxWidth()
            )

            // ¡AQUÍ ESTÁ LA MAGIA! Campo fusionado.
            OutlinedTextField(
                value = "${uiState.equipo?.nombre ?: ""} ${uiState.equipo?.caracteristicas ?: ""}".trim(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Nombre y Características del Equipo") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.equipo?.modelo ?: "Cargando...",
                onValueChange = {},
                readOnly = true,
                label = { Text("Modelo") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f)) // Empuja el botón hacia abajo

            Button(
                onClick = {
                    viewModel.saveLocalTaskDetails { onNextClicked() }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
            ) {
                Text("SIGUIENTE")
            }
        }
    }
}


// --- ASEGÚRATE DE TENER ESTA VERSIÓN DE SearchableDropdown ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableDropdown(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    optionToString: (T) -> String,
    searchText: String = selectedOption?.let(optionToString) ?: "",
    onSearchTextChanged: (String) -> Unit = {},
    enabled: Boolean = true,
    isReadOnly: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    // Cuando se selecciona una opción, el texto del campo debe actualizarse
    val textFieldValue = if (selectedOption != null) optionToString(selectedOption) else searchText

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = {
                if (!isReadOnly) onSearchTextChanged(it)
                expanded = true
            },
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            enabled = enabled,
            readOnly = isReadOnly
        )
        if (expanded) {
            ExposedDropdownMenu(
                expanded = true,
                onDismissRequest = { expanded = false }
            ) {
                val filteredOptions = if (searchText.isEmpty() || isReadOnly) {
                    options
                } else {
                    options.filter { optionToString(it).contains(searchText, ignoreCase = true) }
                }

                if (filteredOptions.isEmpty() && searchText.isNotEmpty() && !isReadOnly) {
                    DropdownMenuItem(text = { Text("No hay resultados") }, enabled = false, onClick = {})
                } else {
                    filteredOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(optionToString(option)) },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

private class PreviewTaskDetailViewModel(initialState: TaskDetailUiState) : TaskDetailViewModel(dao = FakeAppDao()) {
    private val _uiState = MutableStateFlow(initialState)
    override val uiState = _uiState.asStateFlow()
    override fun loadDataForEquipo(equipoId: String) {}
}

@Preview(showBackground = true)
@Composable
fun TaskDetailScreenPreview() {
    val previewState = TaskDetailUiState(
        // ... (Tu estado de ejemplo aquí)
    )
    val previewViewModel = remember { PreviewTaskDetailViewModel(previewState) }

    MantenimientoAppTheme {
        TaskDetailScreen(
            equipoId = "PREVIEW-ID-123",
            numeroSerie = "PREVIEW-ID-123",
            viewModel = previewViewModel,
            onNavigateBack = {},
            onNextClicked = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SimpleDropdown(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    optionToString: (T) -> String,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    // Usamos un Box para superponer el menú sobre el campo de texto.
    Box {
        OutlinedTextField(
            value = selectedOption?.let(optionToString) ?: "",
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = enabled,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Desplegar opciones"
                )
            },
            interactionSource = interactionSource
        )

        // Este es un "detector de clics" invisible que superponemos sobre el campo de texto.
        // Al hacer clic en él, se expande el menú.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // <-- LA SOLUCIÓN DEFINITIVA: Desactiva el ripple conflictivo
                    enabled = enabled,
                    onClick = { expanded = true }
                )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionToString(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}