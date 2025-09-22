// Archivo: MaintenanceActivitiesScreen.kt
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
    onDiagnosticoClicked: () -> Unit,
    onNextClicked: (String, String) -> Unit, // <-- AHORA ESPERA EL EQUIPOID Y EL NUMEROSERIE
    equipoId: String, // <-- PARÁMETRO AÑADIDO
    numeroSerie: String // <-- PARÁMETRO AÑADIDO
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF33A8FF)), // Fondo azul
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
                    // --- AJUSTE 1: Aplicamos el padding aquí ---
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Este contenedor ocupa todo el espacio central
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = onPreventiveClicked,
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
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33A8FF))
                        )
                        {
                            Text("CORRECTIVO")
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onDiagnosticoClicked,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33A8FF))
                        ) {
                            Text("DIAGNÓSTICO")
                        }
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

// DENTRO DE MaintenanceActivitiesScreen.kt

@Preview(showBackground = true)
@Composable
fun MaintenanceActivitiesScreenPreview() {
    MantenimientoAppTheme {
        MaintenanceActivitiesScreen(
            onNavigateBack = {},
            onPreventiveClicked = {},
            onCorrectiveClicked = {},
            onDiagnosticoClicked = {},
            // AJUSTE 1: La función ahora espera dos argumentos, se los damos pero no los usamos.
            onNextClicked = { _, _ -> },
            // AJUSTE 2: Pasamos un ID de equipo ficticio.
            equipoId = "EQUIPO_PRUEBA_ID",
            // AJUSTE 3: Pasamos un número de serie ficticio.
            numeroSerie = "SN_PRUEBA_123"
        )
    }
}