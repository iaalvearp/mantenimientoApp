// Archivo: AddEquipmentScreen.kt
package com.alpes.mantenimientoapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Transaction
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEquipmentScreen(
    viewModel: AddEquipmentViewModel,
    userId: Int,
    onNavigateBack: () -> Unit,
    onEquipoClicked: (equipoId: String, numeroSerie: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = userId) {
        viewModel.loadInitialData(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Equipo Local") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // --- FORMULARIO ---
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.numeroSerie,
                    onValueChange = { viewModel.onNumeroSerieChanged(it) },
                    label = { Text("Número de Serie (ID)") },
                    modifier = Modifier.fillMaxWidth()
                )

                SearchableDropdown(
                    label = "Modelo del Equipo (Buscar)",
                    options = uiState.allModelos,
                    selectedOption = uiState.modelo.takeIf { it.isNotBlank() },
                    onOptionSelected = { viewModel.onModeloSelected(it) },
                    optionToString = { it },
                    searchText = uiState.modelo,
                    onSearchTextChanged = { viewModel.onModeloSearchChanged(it) }
                )

                OutlinedTextField(
                    value = uiState.nombre,
                    onValueChange = {},
                    label = { Text("Nombre del Equipo") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true
                )

                OutlinedTextField(
                    value = uiState.caracteristicas,
                    onValueChange = {},
                    label = { Text("Características") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true
                )

                Button(
                    onClick = { viewModel.saveEquipment(userId) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                    enabled = uiState.numeroSerie.isNotBlank() && uiState.nombre.isNotBlank(),
                ) {
                    Text("GUARDAR EQUIPO NUEVO")
                }
            }

            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Equipos Locales", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(uiState.equiposLocales) { equipo ->
                    EquipmentListItem(
                        equipo = equipo,
                        onClick = { onEquipoClicked(equipo.id, equipo.id) }
                    )
                }
            }
        }
    }
}


// --- VERSIÓN DE SearchableDropdown (LA MANTENEMOS AQUÍ POR AHORA) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableDropdown(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    optionToString: (T) -> String = { it.toString() },
    enabled: Boolean = true,
    searchText: String = selectedOption?.let(optionToString) ?: "",
    onSearchTextChanged: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { newValue ->
                onSearchTextChanged(newValue)
                expanded = true
            },
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            enabled = enabled
        )

        val filteredOptions = options.filter {
            optionToString(it).contains(searchText, ignoreCase = true)
        }

        if (expanded && filteredOptions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionToString(option)) },
                        onClick = {
                            onOptionSelected(option)
                            onSearchTextChanged(optionToString(option))
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AddEquipmentScreenPreview() {
    val previewViewModel = remember { AddEquipmentViewModel(FakeAppDao()) }
    MantenimientoAppTheme {
        AddEquipmentScreen(
            viewModel = previewViewModel,
            userId = 101,
            onNavigateBack = {},
            onEquipoClicked = { _, _ -> }
        )
    }
}