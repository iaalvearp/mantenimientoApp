// Archivo: AddEquipmentScreen.kt
package com.alpes.mantenimientoapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEquipmentScreen(
    viewModel: AddEquipmentViewModel,
    onNavigateBack: () -> Unit,
    onEquipoClicked: (equipoId: String, numeroSerie: String) -> Unit // Para navegar al detalle
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = Unit) {
        viewModel.loadLocalEquipment()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF33A8FF)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Scaffold(
                containerColor = Color.White,
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
                        OutlinedTextField(
                            value = uiState.nombre,
                            onValueChange = { viewModel.onNombreChanged(it) },
                            label = { Text("Nombre del Equipo") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = uiState.modelo,
                            onValueChange = { viewModel.onModeloChanged(it) },
                            label = { Text("Modelo") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = uiState.caracteristicas,
                            onValueChange = { viewModel.onCaracteristicasChanged(it) },
                            label = { Text("Características") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { viewModel.saveEquipment() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = uiState.numeroSerie.isNotBlank() && uiState.nombre.isNotBlank(),
                            // --- TU MEJORA DE ESTILO, ¡INTEGRADA! ---
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.numeroSerie.isNotBlank() && uiState.nombre.isNotBlank()) Color(0xFFF57C00) else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("GUARDAR EQUIPO NUEVO")
                        }
                    }

                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Equipos Locales", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(uiState.equiposLocales) { equipo ->
                            EquipmentListItem(
                                equipo = equipo,
                                // --- ¡AQUÍ ESTÁ LA NUEVA LÓGICA! ---
                                // Al hacer clic, navega al formulario principal
                                onClick = { onEquipoClicked(equipo.id, equipo.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}