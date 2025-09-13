// Archivo: TaskDetailScreen.kt
package com.alpes.mantenimientoapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    equipoId: String,
    viewModel: TaskDetailViewModel,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(key1 = equipoId) {
        viewModel.loadDataForEquipo(equipoId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val equipo = uiState.equipo
    val tarea = uiState.tarea

    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

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
                        title = { Text("Ficha de Mantenimiento") },
                        navigationIcon = {
                            IconButton(onClick = { /* TODO: Lógica para volver atrás */ }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                        .padding(horizontal = 8.dp, vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("INFORMACIÓN GENERAL", style = MaterialTheme.typography.titleMedium)
                        DropdownField(label = "Cliente", options = listOf("CORPORACIÓN NACIONAL DE ELECTRICIDAD CNEL EP"), isTall = true)
                        DropdownField(label = "Nombre del proyecto", options = listOf("CORP SERVICIO DE SOPORTE MANTENIMIENTO Y GARANTÍA..."), isTall = true)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) { DropdownField(label = "Provincia", options = listOf("ESMERALDAS", "GUAYAS")) }
                            Box(modifier = Modifier.weight(1f)) { DropdownField(label = "Ciudad", options = listOf("ESMERALDAS", "GUAYAQUIL")) }
                        }
                        DropdownField(label = "Unidad de negocio", options = listOf("ESMERALDAS"))
                        DateField(selectedDateMillis = selectedDateMillis, onClick = { showDatePicker = true })
                        DropdownField(label = "Agencia, oficina, subestación", options = listOf("UBICACIÓN ACTUAL SUBESTACIÓN MANTA 1..."), isTall = true)

                        Text("INFORMACIÓN DE EQUIPO", style = MaterialTheme.typography.titleMedium)
                        DropdownField(
                            label = "Tipo de equipo",
                            options = listOf(equipo?.nombre ?: "Cargando..."),
                        )
                        DropdownField(
                            label = "Número de serie",
                            options = listOf(equipo?.id ?: "Cargando..."),
                        )
                        DropdownField(
                            label = "Modelo del equipo",
                            options = listOf(equipo?.modelo ?: "Cargando..."),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { /*TODO*/ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                    ) {
                        Text("SIGUIENTE")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// --- FUNCIONES AUXILIARES ---
// Aquí estaban las definiciones que se borraron.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    options: List<String>,
    initialValue: String = options.firstOrNull() ?: "",
    isTall: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(initialValue) }
    val heightModifier = if (isTall) Modifier.heightIn(min = 80.dp) else Modifier

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .then(heightModifier),
            readOnly = true,
            value = selectedOptionText,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(unfocusedContainerColor = Color(0xFFFFFFFF)),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        selectedOptionText = selectionOption
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DateField(selectedDateMillis: Long, onClick: () -> Unit) {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateString = formatter.format(Date(selectedDateMillis))

    OutlinedTextField(
        value = dateString,
        onValueChange = {},
        label = { Text("Fecha") },
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
    // 1. Creamos un contexto falso para el ViewModel.
    val context = LocalContext.current
    // 2. Creamos un DAO falso (no lo usaremos, pero el ViewModel lo necesita).
    val fakeDao = AppDatabase.getDatabase(context).appDao()
    // 3. Creamos una instancia del ViewModel real, pero solo para el preview.
    val previewViewModel = TaskDetailViewModel(fakeDao)

    MantenimientoAppTheme {
        TaskDetailScreen(
            equipoId = "PREVIEW-ID-123", // Un ID de ejemplo
            viewModel = previewViewModel,   // El ViewModel que acabamos de crear
            onNavigateBack = {}             // Una función vacía, ya que no hay a dónde navegar
        )
    }
}