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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlin.OptIn // <-- Y esta también

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalizacionScreen(
    equipoId: String,
    numeroSerie: String,
    viewModel: FinalizacionViewModel,
    onNavigateBackToHome: (Int) -> Unit,
    onBackClicked: () -> Unit // Este parámetro ahora lo usaremos
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

    Scaffold(
        topBar = {
            // AJUSTE 1: Añadimos la barra superior
            TopAppBar(
                title = { Text("Finalización de Mantenimiento") },
                // AJUSTE 2: Añadimos el icono de navegación (la flecha)
                navigationIcon = {
                    IconButton(onClick = onBackClicked) { // Conectamos la acción de volver
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Asegúrate de importar el icono
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White, // Fondo blanco para la barra
                    titleContentColor = Color.Black // Texto negro
                )
            )
        },
        containerColor = Color(0xFF33A8FF) // Mantenemos el fondo azul general de la pantalla
    ) { paddingValues -> // El Scaffold nos da un padding para respetar la TopAppBar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // AJUSTE 3: Aplicamos el padding aquí
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
                    // El Text del título ya no es necesario aquí, porque está en la TopAppBar

                    Spacer(modifier = Modifier.height(16.dp))

                    InfoRow(label = "Técnico Responsable:", value = uiState.tecnico?.nombre ?: "Cargando...")
                    InfoRow(label = "Cliente:", value = uiState.cliente?.nombreCompleto ?: "Cargando...")

                    Spacer(modifier = Modifier.height(16.dp))

                    // Usamos el estilo de InfoRow para el campo del delegado
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Delegado:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        OutlinedTextField(
                            value = uiState.responsableCliente,
                            onValueChange = { viewModel.onResponsableChanged(it) },
                            label = { Text("Nombre del Responsable del Cliente") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f)) // Empuja el botón hacia abajo

                    Button(
                        onClick = { viewModel.saveAndFinalize(equipoId, numeroSerie) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = uiState.responsableCliente.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.responsableCliente.isNotBlank()) Color(0xFFF57C00) else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (uiState.responsableCliente.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("FINALIZAR")
                    }
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