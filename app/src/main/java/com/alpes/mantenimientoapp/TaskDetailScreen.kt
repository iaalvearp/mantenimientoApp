// Archivo: TaskDetailScreen.kt
package com.alpes.mantenimientoapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    // Más adelante recibiremos el ViewModel y el NavController
) {
    // Estado para la fecha, inicializado con el timestamp actual
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ficha de Mantenimiento") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Lógica para volver atrás */ }) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("INFORMACIÓN GENERAL", style = MaterialTheme.typography.titleMedium)

            // --- Campos de Información General ---
            DropdownField(label = "Cliente", options = listOf("CORPORACIÓN NACIONAL DE ELECTRICIDAD CNEL EP"))
            DropdownField(label = "Nombre del proyecto", options = listOf("CORP SERVICIO DE SOPORTE MANTENIMIENTO Y GARANTÍA..."))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    DropdownField(label = "Provincia", options = listOf("ESMERALDAS", "GUAYAS", "PICHINCHA"))
                }
                Box(modifier = Modifier.weight(1f)) {
                    DropdownField(label = "Ciudad", options = listOf("ESMERALDAS", "GUAYAQUIL", "QUITO"))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    DropdownField(label = "Nombre de unidad de negocio", options = listOf("ESMERALDAS"))
                }
                Box(modifier = Modifier.weight(1f)) {
                    DateField(
                        label = "Fecha",
                        selectedDateMillis = selectedDateMillis,
                        onClick = { showDatePicker = true }
                    )
                }
            }

            DropdownField(label = "Agencia, oficina, subestación", options = listOf("UBICACIÓN ACTUAL SUBESTACIÓN MANTA 1 - BODEGA DE TRANSFORMADORES"))

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))) {
                Text("SIGUIENTE")
            }
        }
    }

    // --- Diálogo del Selector de Fecha ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    options: List<String>,
    initialValue: String = options.firstOrNull() ?: ""
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(initialValue) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .heightIn(max = 150.dp), // Máximo 3 líneas aprox.
            readOnly = true,
            value = selectedOptionText,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        selectedOptionText = selectionOption
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun DateField(
    label: String,
    selectedDateMillis: Long,
    onClick: () -> Unit
) {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateString = formatter.format(Date(selectedDateMillis))

    OutlinedTextField(
        value = dateString,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha", Modifier.clickable(onClick = onClick))
        }
    )
}

@Preview(showBackground = true)
@Composable
fun TaskDetailScreenPreview() {
    MantenimientoAppTheme {
        TaskDetailScreen()
    }
}