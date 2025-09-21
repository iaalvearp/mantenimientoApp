// Archivo: FinalizacionScreen.kt
package com.alpes.mantenimientoapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FinalizacionScreen(
    equipoId: String,
    viewModel: FinalizacionViewModel,
    onNavigateBackToHome: (Int) -> Unit // <-- AHORA ESPERA UN INT (el userId)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Se ejecuta una sola vez para cargar los datos
    LaunchedEffect(key1 = equipoId) {
        viewModel.loadData(equipoId)
    }

    // Observa si la finalización fue exitosa para navegar
    LaunchedEffect(key1 = uiState.finalizacionExitosa) {
        if (uiState.finalizacionExitosa) {
            // ¡CAMBIO AQUÍ! Le pasamos el userId que obtuvimos del estado.
            onNavigateBackToHome(uiState.userId)
        }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Finalización de Mantenimiento", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(24.dp))

                InfoRow(label = "Técnico Responsable:", value = uiState.tecnico?.nombre ?: "Cargando...")
                InfoRow(label = "Cliente:", value = uiState.cliente?.nombreCompleto ?: "Cargando...")

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.responsableCliente,
                    onValueChange = { viewModel.onResponsableChanged(it) },
                    label = { Text("Nombre del Responsable del Cliente") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f)) // Empuja el botón hacia abajo

                Button(
                    onClick = { viewModel.saveAndFinalize(equipoId) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.responsableCliente.isNotBlank() // Solo se activa si se escribe un nombre
                ) {
                    Text("FINALIZAR")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
    }
}