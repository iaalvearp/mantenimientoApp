// Archivo: MaintenanceActivitiesScreen.kt (REEMPLAZAR COMPLETO)
package com.alpes.mantenimientoapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceActivitiesScreen(
    onNavigateBack: () -> Unit,
    onPreventiveClicked: () -> Unit,
    onCorrectiveClicked: () -> Unit,
    // El onDiagnosticoClicked se elimina
    onNextClicked: (String, String) -> Unit,
    equipoId: String,
    numeroSerie: String,
    // --- NUEVOS PARÁMETROS PARA DESACTIVAR BOTONES ---
    isPreventiveEnabled: Boolean = true,
    isCorrectiveEnabled: Boolean = true
) {
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
                        title = { Text("Actividades de Mantenimiento") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = onPreventiveClicked,
                            // --- LÓGICA PARA DESACTIVAR EL BOTÓN ---
                            enabled = isPreventiveEnabled,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33A8FF))
                        )
                        {
                            Text("PREVENTIVO")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCorrectiveClicked,
                            // --- LÓGICA PARA DESACTIVAR EL BOTÓN ---
                            enabled = isCorrectiveEnabled,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33A8FF))
                        )
                        {
                            Text("CORRECTIVO")
                        }
                        // --- BOTÓN DE DIAGNÓSTICO ELIMINADO ---
                    }

                    // Botón "Siguiente" en la parte inferior
                    Button(
                        onClick = { onNextClicked(equipoId, numeroSerie) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                    ) {
                        Text("SIGUIENTE")
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MaintenanceActivitiesScreenPreview() {
    MantenimientoAppTheme {
        MaintenanceActivitiesScreen(
            onNavigateBack = {},
            onPreventiveClicked = {},
            onCorrectiveClicked = {},
            onNextClicked = { _, _ -> },
            equipoId = "EQUIPO_PRUEBA_ID",
            numeroSerie = "SN_PRUEBA_123",
            // Valores de ejemplo para la preview
            isPreventiveEnabled = true,
            isCorrectiveEnabled = false // Ejemplo de un botón desactivado
        )
    }
}