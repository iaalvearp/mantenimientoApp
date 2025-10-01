// Archivo: TaskDetailScreen.kt
package com.alpes.mantenimientoapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import java.util.*

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
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("INFORMACIÓN GENERAL", style = MaterialTheme.typography.titleMedium)

            SearchableDropdown(label = "Cliente", options = uiState.allClientes, selectedOption = uiState.clienteSeleccionado, onOptionSelected = { viewModel.onClienteSelected(it) }, optionToString = { it.nombreCompleto }, isReadOnly = true)
            SearchableDropdown(label = "Nombre del proyecto", options = uiState.proyectosFiltrados, selectedOption = uiState.proyectoSeleccionado, onOptionSelected = { viewModel.onProyectoSelected(it) }, optionToString = { it.nombre }, enabled = uiState.clienteSeleccionado != null, isReadOnly = true)
            SearchableDropdown(label = "Provincia", options = uiState.allProvincias, selectedOption = uiState.provinciaSeleccionada, onOptionSelected = { viewModel.onProvinciaSelected(it) }, optionToString = { it.nombre }, isReadOnly = true)
            SearchableDropdown(label = "Ciudad", options = uiState.ciudadesFiltradas, selectedOption = uiState.ciudadSeleccionada, onOptionSelected = { viewModel.onCiudadSelected(it) }, optionToString = { it.nombre }, enabled = uiState.provinciaSeleccionada != null, searchText = uiState.ciudadSearchText, onSearchTextChanged = { viewModel.onCiudadSearchTextChanged(it) })
            SearchableDropdown(label = "Unidad de Negocio", options = uiState.allUnidadesNegocio, selectedOption = uiState.unidadNegocioSeleccionada, onOptionSelected = { viewModel.onUnidadNegocioSelected(it) }, optionToString = { it.nombre }, isReadOnly = true)
            SearchableDropdown(label = "Localidad, oficina, subestación", options = uiState.agenciasFiltradas, selectedOption = uiState.agenciaSeleccionada, onOptionSelected = { viewModel.onAgenciaSelected(it) }, optionToString = { it.nombre }, enabled = uiState.ciudadSeleccionada != null)

            Spacer(modifier = Modifier.height(16.dp))
            Text("INFORMACIÓN DE EQUIPO", style = MaterialTheme.typography.titleMedium)

            val tipoEquipoText = listOfNotNull(uiState.equipo?.nombre, uiState.equipo?.caracteristicas?.takeIf { it.isNotBlank() }).joinToString(" ")
            OutlinedTextField(value = tipoEquipoText.ifEmpty { "N/A" }, onValueChange = {}, readOnly = true, label = { Text("Tipo de equipo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = uiState.equipo?.id ?: "N/A", onValueChange = {}, readOnly = true, label = { Text("Número de serie") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = uiState.equipo?.modelo ?: "N/A", onValueChange = {}, readOnly = true, label = { Text("Modelo del equipo") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onNextClicked,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
            ) { Text("SIGUIENTE") }
        }
    }
}

// --- VERSIÓN FINAL Y ÚNICA DE SearchableDropdown ---
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

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = if (isReadOnly) selectedOption?.let(optionToString) ?: "" else searchText,
            onValueChange = {
                if (!isReadOnly) {
                    onSearchTextChanged(it)
                    expanded = true
                }
            },
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            enabled = enabled,
            readOnly = isReadOnly
        )
        if (expanded) {
            ExposedDropdownMenu(
                expanded = true,
                onDismissRequest = { expanded = false }
            ) {
                // Si el campo de búsqueda está vacío, o es de solo lectura, mostramos todas las opciones.
                // Si no, filtramos.
                val filteredOptions = if (searchText.isEmpty() || isReadOnly) {
                    options
                } else {
                    options.filter { optionToString(it).contains(searchText, ignoreCase = true) }
                }

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