// Archivo: MaintenanceActivitiesScreen.kt
package com.alpes.mantenimientoapp

import androidx.compose.foundation.layout.*
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
    onNextClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actividades de Mantenimiento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onPreventiveClicked,
                modifier = Modifier.fillMaxWidth(),
                // CORRECCIÓN: Nuevo color para el botón PREVENTIVO
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF049AF8))
            ) {
                Text("PREVENTIVO")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCorrectiveClicked,
                modifier = Modifier.fillMaxWidth(),
                // CORRECCIÓN: Nuevo color para el botón CORRECTIVO
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF049AF8))
            ) {
                Text("CORRECTIVO")
            }

            // Fila para los botones de navegación inferiores
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp) // Añade espacio entre los botones
            ) {
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    // CORRECCIÓN: Usamos weight para que ocupe la mitad del espacio
                    modifier = Modifier.weight(1f)
                ) {
                    Text("ATRÁS")
                }
                Button(
                    onClick = onNextClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                    // CORRECCIÓN: Usamos weight para que ocupe la otra mitad
                    modifier = Modifier.weight(1f)
                ) {
                    Text("SIGUIENTE")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MaintenanceActivitiesScreenPreview() {
    MantenimientoAppTheme {
        MaintenanceActivitiesScreen({}, {}, {}, {})
    }
}